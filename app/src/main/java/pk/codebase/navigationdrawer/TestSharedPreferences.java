package pk.codebase.navigationdrawer;


import android.content.Context;
import android.content.SharedPreferences;

public class TestSharedPreferences {

        private static final String PREF_NAME = "TestPref";
        private static final String KEY_TEST_WORD = "test_word";

        // Method to save a word in SharedPreferences
        public static void saveWord(Context context, String word) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_TEST_WORD, word);
            editor.apply();
        }

        // Method to retrieve the word from SharedPreferences
        public static String getWord(Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return sharedPreferences.getString(KEY_TEST_WORD, "");
        }
    }
