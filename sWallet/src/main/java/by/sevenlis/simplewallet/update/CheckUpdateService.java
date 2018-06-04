package by.sevenlis.simplewallet.update;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;

import by.sevenlis.simplewallet.R;
import by.sevenlis.simplewallet.utils.NetworkUtil;

public class CheckUpdateService extends Service {
    private static final double currentVersion = 1.01d;
    private static double remoteVersion = currentVersion;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (NetworkUtil.getConnectivityStatus(getApplicationContext()) != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
            new GetRemoteVersion().execute();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    private void sendUpdateAvailableNotification(String newVersion) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(),"UPDATE")
                .setTicker(getString(R.string.update_available))
                .setContentTitle(getString(R.string.update_available))
                .setContentText(MessageFormat.format(getString(R.string.update_to_version),newVersion))
                .setSmallIcon(R.drawable.ic_action_info)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setAutoCancel(true);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .setBigContentTitle(getString(R.string.update_available))
                .bigText(MessageFormat.format(getString(R.string.update_to_version),newVersion));
        mBuilder.setStyle(bigTextStyle);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setSubText(getString(R.string.press_to_start_update));
        
        Intent intent = new Intent(getApplicationContext(),UpdateApkIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
    
        notificationManager.notify(0, mBuilder.build());
    }
    
    @SuppressLint("StaticFieldLeak")
    class GetRemoteVersion extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                getRemoteVersion();
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            return null;
        }
    
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (remoteVersion > currentVersion) {
                sendUpdateAvailableNotification(String.valueOf(remoteVersion));
            }
        }
    
        private void getRemoteVersion() throws IOException {
            String sVersion = String.valueOf(currentVersion);
            
            HttpURLConnection connection;
            URL url = new URL("https://www.dropbox.com/s/7u0b4fooc1upssj/sWallet-version.txt?raw=1");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return;
            }
    
            File versionInfoFile = new File(String.format("%s%s%s",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),File.separator,"version.info"));
            OutputStream out = new FileOutputStream(versionInfoFile);
            InputStream in = connection.getInputStream();
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data)) != -1) {
                out.write(data, 0, count);
            }
            out.flush();
            out.close();
    
            BufferedReader bufferedReader = new BufferedReader(new FileReader(versionInfoFile));
            String str;
            if ((str = bufferedReader.readLine()) != null) {
                sVersion = str;
            }
            bufferedReader.close();
            
            try {
                remoteVersion = Double.valueOf(sVersion);
            } catch (NumberFormatException e) {
                remoteVersion = currentVersion;
            }
            
            if (versionInfoFile.exists()) {
                if (!versionInfoFile.delete()) {
                    Toast.makeText(CheckUpdateService.this, "Error deleting temporary file with version info:\n" + versionInfoFile.getPath(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
