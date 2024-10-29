package com.lin.magic.browser.theme

import com.lin.magic.utils.ThemeUtils
import android.app.Activity
import javax.inject.Inject

/**
 * The default theme attribute provider that delegates to the activity.
 */
class DefaultThemeProvider @Inject constructor(private val activity: Activity) : ThemeProvider {

    override fun color(attrRes: Int): Int =
        ThemeUtils.getColor(activity, attrRes)

}
