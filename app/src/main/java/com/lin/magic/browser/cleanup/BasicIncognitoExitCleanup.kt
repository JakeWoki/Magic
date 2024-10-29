package com.lin.magic.browser.cleanup

import com.lin.magic.utils.WebUtils
import javax.inject.Inject

/**
 * Exit cleanup that should run on API < 28 when the incognito instance is closed. This is
 * significantly less secure than on API > 28 since we can separate WebView data from
 */
class BasicIncognitoExitCleanup @Inject constructor() : ExitCleanup {
    override fun cleanUp() {
        // We want to make sure incognito mode is secure as possible without also breaking existing
        // browser instances.
        WebUtils.clearWebStorage()
    }
}
