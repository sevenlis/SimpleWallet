package by.sevenlis.simplewallet.classes;

import android.content.Context;
import android.preference.PreferenceManager;

public class Settings {
    public static boolean getExpandListOnLoad(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_expand_list_on_load",false);
    }
    public static int getListFontSize(Context context) {
        return Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_list_font_size","18"));
    }
}
