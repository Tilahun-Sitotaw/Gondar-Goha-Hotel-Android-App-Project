package com.gohahotel.connect.core.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    /**
     * Applies the selected language to the app.
     * On Android 13 (API 33) and above, it uses the new LocaleManager.
     * On older versions, it uses AppCompatDelegate.
     */
    fun applyLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        } else {
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    /**
     * Returns the current app language code.
     */
    fun getLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales[0]?.language ?: "en"
        } else {
            AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"
        }
    }
}
