package com.example.mosquizto.Util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.mosquizto.R;

import java.util.Locale;

public final class LocaleManager {

    public static final String LANG_EN = "en";
    public static final String LANG_VI = "vi";

    private static final String PREF_NAME = "mosquizto_locale";
    private static final String KEY_LANGUAGE = "language_tag";

    private LocaleManager() {
    }

    public static void applySavedLocale(Context context) {
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(getLanguageTag(context))
        );
    }

    public static String getLanguageTag(Context context) {
        return getPreferences(context).getString(KEY_LANGUAGE, getDefaultTag());
    }

    public static void setLanguage(Context context, String languageTag) {
        String tag = LANG_VI.equals(languageTag) ? LANG_VI : LANG_EN;
        getPreferences(context)
                .edit()
                .putString(KEY_LANGUAGE, tag)
                .apply();
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
    }

    public static String getLanguageDisplayName(Context context, String languageTag) {
        if (LANG_VI.equals(languageTag)) {
            return context.getString(R.string.language_name_vietnamese);
        }
        return context.getString(R.string.language_name_english);
    }

    private static String getDefaultTag() {
        return "vi".equals(Locale.getDefault().getLanguage()) ? LANG_VI : LANG_EN;
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
