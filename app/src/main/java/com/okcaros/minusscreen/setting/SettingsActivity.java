package com.okcaros.minusscreen.setting;

import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.okcaros.minusscreen.R;

import java.util.ArrayList;
import java.util.Arrays;
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
        List<CharSequence> appNames = new ArrayList<>();
        List<CharSequence> packageNames = new ArrayList<>();
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.setting_preferences, rootKey);

            String[] preferenceKeyList = {
                    getResources().getString(R.string.preference_key_map),
                    getResources().getString(R.string.preference_key_music),
                    getResources().getString(R.string.preference_key_weather)
            };

            getInstalledAppList();

            if (appNames.size() == 0) {
                AlertDialog.Builder noAppAlert = new AlertDialog.Builder(getContext());
                noAppAlert.setTitle(getResources().getString(R.string.freeformApp));
                noAppAlert.setMessage(getResources().getString(R.string.noAppTip));
                noAppAlert.setPositiveButton(getResources().getString(R.string.confirm), null);
                noAppAlert.show();
            }

            for (int i = 0; i < preferenceKeyList.length; i++) {
                ListPreference appListPreference = findPreference(preferenceKeyList[i]);

                appListPreference.setEntries(appNames.toArray(new CharSequence[0]));
                appListPreference.setEntryValues(packageNames.toArray(new CharSequence[0]));
                appListPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(@NonNull Preference preference) {
                        getInstalledAppList();

                        appListPreference.setEntries(appNames.toArray(new CharSequence[0]));
                        appListPreference.setEntryValues(packageNames.toArray(new CharSequence[0]));
                        return false;
                    }
                });
            }
        }

        public void getInstalledAppList() {
            installedAppList = packageManager.getInstalledApplications(0);

            appNames.clear();
            packageNames.clear();
            for (ApplicationInfo appInfo : installedAppList) {
                // 过滤系统应用
                if (((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) | containsString(appInfo.packageName)) {
                    appNames.add(appInfo.loadLabel(packageManager));
                    packageNames.add(appInfo.packageName);
                }
            }
        }
    }

    public static boolean containsString(String target) {
        String[] systemAppWhiteListArray = {
                "com.android.deskclock",
                "com.google.android.apps.maps",
                "com.android.calendar",
                "com.google.android.youtube"
        };

        List<String> list = Arrays.asList(systemAppWhiteListArray);
        return list.contains(target);
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