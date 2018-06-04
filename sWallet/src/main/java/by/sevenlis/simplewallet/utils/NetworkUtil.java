package by.sevenlis.simplewallet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
    public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    private static final int NETWORK_STATUS_WIFI = 1;
    private static final int NETWORK_STATUS_MOBILE = 2;
    
    private static final int TYPE_NOT_CONNECTED = 0;
    private static final int TYPE_WIFI = 1;
    private static final int TYPE_MOBILE = 2;
    
    private static int getConnectivityType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }
    
    public static int getConnectivityStatus(Context context) {
        int conn = NetworkUtil.getConnectivityType(context);
        int status = NETWORK_STATUS_NOT_CONNECTED;
        if (conn == NetworkUtil.TYPE_WIFI) {
            status = NETWORK_STATUS_WIFI;
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = NETWORK_STATUS_MOBILE;
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = NETWORK_STATUS_NOT_CONNECTED;
        }
        return status;
    }
}
