package by.sevenlis.simplewallet.update;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import by.sevenlis.simplewallet.R;

public class UpdateApkIntentService extends IntentService {
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notificationUpdateBuilder;
    
    public UpdateApkIntentService() {
        super("UpdateApkIntentService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            notificationUpdateBuilder = new NotificationCompat.Builder(getApplicationContext(),"UPDATE")
                    .setTicker(getString(R.string.update_downloading))
                    .setContentTitle(getString(R.string.update_downloading))
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setOngoing(true)
                    .setUsesChronometer(false);
            notificationManager = getNotificationManager();
            startApkUpdate();
        }
    }
    
    private NotificationManagerCompat getNotificationManager() {
        if (notificationManager == null)
            notificationManager = NotificationManagerCompat.from(getApplicationContext());
        return notificationManager;
    }
    
    private void notifyUpdate(int progress) {
        notificationUpdateBuilder.setProgress(100, progress, false);
        assert notificationManager != null;
        notificationManager.notify(0, notificationUpdateBuilder.build());
    }
    
    private void startApkUpdate() {
        final int batchSize = 4096;
        //final int progressSizeDivider = 1024;
        final File apkFile = new File(String.format("%s%s%s", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File.separator, "sWallet.apk"));
    
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            //URL url = new URL("https://www.dropbox.com/s/ergc30fcbesu6i9/sWallet-release.apk?raw=1");
            URL url = new URL("https://www.dropbox.com/s/t7l4dr7hp3ko822/sWallet-debug.apk?raw=1");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
        
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return;
            }
        
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            notifyUpdate(0);
        
            // download the file
            boolean deleted = true;
            boolean created = true;
            input = connection.getInputStream();
            if (apkFile.exists()) {
                deleted = apkFile.delete();
            }
            if (deleted) {
                created = apkFile.createNewFile();
            }
            if (!created) {
                return;
            }
            output = new FileOutputStream(apkFile);
    
        
            byte data[] = new byte[batchSize];
            int count, countTotal = 0;
            while ((count = input.read(data)) != -1) {
                // publishing the progress...
                if (fileLength > 0) { // only if total length is known
                    countTotal += count;
                    
                    int progress = (int) ((double) countTotal / (double) fileLength * 100);
                    if (progress % 10 == 0)
                        notifyUpdate(progress);
                }
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
                //do nothing
            }
        
            if (connection != null) connection.disconnect();
    
            assert notificationManager != null;
            notificationManager.cancel(0);
        }
    
        if (apkFile != null && apkFile.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = GenericFileProvider.getUriForFile(getApplicationContext(),getApplicationContext().getPackageName() + ".file.provider",apkFile);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } else {
                Uri apkUri = Uri.fromFile(apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
    
}
