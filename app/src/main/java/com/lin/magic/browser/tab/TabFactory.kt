package com.lin.magic.browser.tab

import com.lin.magic.browser.image.IconFreeze
import com.lin.magic.preference.UserPreferences
import android.graphics.Bitmap
import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Provider

/**
 * Constructs a [TabModel].
 */
class TabFactory @Inject constructor(
    private val webViewFactory: WebViewFactory,
    private val userPreferences: UserPreferences,
    @DefaultUserAgent private val defaultUserAgent: String,
    @DefaultTabTitle private val defaultTabTitle: String,
    @IconFreeze private val iconFreeze: Bitmap,
    private val tabWebViewClientFactory: TabWebViewClient.Factory,
    private val tabWebChromeClientProvider: Provider<TabWebChromeClient>
) {

    /**
     * Constructs a tab from the [webView] with the provided [tabInitializer].
     */
    fun constructTab(tabInitializer: TabInitializer, webView: WebView): TabModel {
        val headers = webViewFactory.createRequestHeaders()
        return TabAdapter(
            tabInitializer = tabInitializer,
            webView = webView,
            requestHeaders = headers,
            tabWebViewClient = tabWebViewClientFactory.create(headers),
            tabWebChromeClient = tabWebChromeClientProvider.get(),
            userPreferences = userPreferences,
            defaultUserAgent = defaultUserAgent,
            defaultTabTitle = defaultTabTitle,
            iconFreeze = iconFreeze
        )
    }
}
