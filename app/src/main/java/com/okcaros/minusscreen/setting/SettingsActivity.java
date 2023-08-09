package com.okcaros.minusscreen.setting;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.okcaros.minusscreen.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    static List<ApplicationInfo> installedAppList = new ArrayList<>();
    static PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        packageManager = getPackageManager();
        installedAppList = packageManager.getInstalledApplications(0);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.setting_preferences, rootKey);

            List<CharSequence> appNames = new ArrayList<>();
            List<CharSequence> packageNames = new ArrayList<>();
            List<Drawable> appIcons = new ArrayList<>();

            for (ApplicationInfo appInfo : installedAppList) {
                // 过滤系统应用
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    Log.e("minusscreen", appInfo.packageName);
                    appNames.add(appInfo.loadLabel(packageManager));
                    packageNames.add(appInfo.packageName);
                    appIcons.add(appInfo.loadIcon(packageManager));
                }
            }

            String[] preferenceKeyList = {
                getResources().getString(R.string.preference_key_map),
                getResources().getString(R.string.preference_key_music),
            };
            for (int i = 0; i < preferenceKeyList.length; i++) {
                ListPreference appListPreference = findPreference(preferenceKeyList[i]);
                appListPreference.setEntries(appNames.toArray(new CharSequence[0]));
                appListPreference.setEntryValues(packageNames.toArray(new CharSequence[0]));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}