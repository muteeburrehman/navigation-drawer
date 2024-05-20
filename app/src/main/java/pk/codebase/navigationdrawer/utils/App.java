package pk.codebase.navigationdrawer.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class App extends Application {
    public static Context context;

    public static final String PREF_PUBLIC_KEY = "public_key";
    public static final String PREF_PRIVATE_KEY = "private_key";


    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static SharedPreferences getPreferenceManager() {
        return getContext().getSharedPreferences("shared_prefs", MODE_PRIVATE);
    }

    public static void saveString(String key, String value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static String getString(String key) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(key, "");
    }

    public static void saveBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(key, false);
    }


    public static void removeKey(String key) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().remove(key).apply();
    }

}
