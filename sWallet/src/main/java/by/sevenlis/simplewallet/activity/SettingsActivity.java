package by.sevenlis.simplewallet.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import by.sevenlis.simplewallet.R;

public class SettingsActivity extends AppCompatActivity {
    static Preference.OnPreferenceChangeListener prefFontSizeChangeListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, settingsFragment).commit();
    
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(R.string.action_settings);
        }
        
        prefFontSizeChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                setResult(RESULT_OK);
                return true;
            }
        };
    }
    
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_fragment);
            
            Preference fontSizePreference = findPreference("pref_list_font_size");
            fontSizePreference.setOnPreferenceChangeListener(prefFontSizeChangeListener);
        }
    
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    
        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    
    }
}
