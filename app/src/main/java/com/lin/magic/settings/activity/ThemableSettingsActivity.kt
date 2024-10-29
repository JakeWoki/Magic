package com.lin.magic.settings.activity

import com.lin.magic.AppTheme
import com.lin.magic.R
import com.lin.magic.browser.di.injector
import com.lin.magic.preference.UserPreferences
import com.lin.magic.utils.ThemeUtils
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

abstract class ThemableSettingsActivity : AppCompatActivity() {

    private var themeId: AppTheme = AppTheme.LIGHT

    @Inject internal lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        themeId = userPreferences.useTheme

        // set the theme
        when (themeId) {
            AppTheme.LIGHT -> {
                setTheme(R.style.Theme_SettingsTheme)
                window.setBackgroundDrawable(ColorDrawable(ThemeUtils.getPrimaryColor(this)))
            }
            AppTheme.DARK -> {
                setTheme(R.style.Theme_SettingsTheme_Dark)
                window.setBackgroundDrawable(ColorDrawable(ThemeUtils.getPrimaryColorDark(this)))
            }
            AppTheme.BLACK -> {
                setTheme(R.style.Theme_SettingsTheme_Black)
                window.setBackgroundDrawable(ColorDrawable(ThemeUtils.getPrimaryColorDark(this)))
            }
        }
        super.onCreate(savedInstanceState)

        resetPreferences()
    }

    private fun resetPreferences() {
        if (userPreferences.useBlackStatusBar) {
            window.statusBarColor = Color.BLACK
        } else {
            window.statusBarColor = ThemeUtils.getStatusBarColor(this)
        }
    }

    override fun onResume() {
        super.onResume()
        resetPreferences()
        if (userPreferences.useTheme != themeId) {
            recreate()
        }
    }

}
