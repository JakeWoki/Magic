package com.lin.magic.browser.tab.bundle

import com.lin.magic.R
import com.lin.magic.browser.di.DiskScheduler
import com.lin.magic.browser.tab.BookmarkPageInitializer
import com.lin.magic.browser.tab.DownloadPageInitializer
import com.lin.magic.browser.tab.FreezableBundleInitializer
import com.lin.magic.browser.tab.HistoryPageInitializer
import com.lin.magic.browser.tab.HomePageInitializer
import com.lin.magic.browser.tab.TabInitializer
import com.lin.magic.browser.tab.TabModel
import com.lin.magic.utils.FileUtils
import com.lin.magic.utils.isBookmarkUrl
import com.lin.magic.utils.isDownloadsUrl
import com.lin.magic.utils.isHistoryUrl
import com.lin.magic.utils.isSpecialUrl
import com.lin.magic.utils.isStartPageUrl
import android.app.Application
import android.os.Bundle
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

/**
 * A bundle store that serializes each tab state to disk and supports its retrieval.
 */
class DefaultBundleStore @Inject constructor(
    private val application: Application,
    private val bookmarkPageInitializer: BookmarkPageInitializer,
    private val homePageInitializer: HomePageInitializer,
    private val downloadPageInitializer: DownloadPageInitializer,
    private val historyPageInitializer: HistoryPageInitializer,
    @DiskScheduler private val diskScheduler: Scheduler
) : BundleStore {

    override fun save(tabs: List<TabModel>) {
        val outState = Bundle(ClassLoader.getSystemClassLoader())

        tabs.withIndex().forEach { (index, tab) ->
            if (!tab.url.isSpecialUrl()) {
                outState.putBundle(BUNDLE_KEY + index, tab.freeze())
                outState.putString(TAB_TITLE_KEY + index, tab.title)
            } else {
                outState.putBundle(BUNDLE_KEY + index, Bundle().apply {
                    putString(URL_KEY, tab.url)
                })
            }
        }

        FileUtils.writeBundleToStorage(application, outState, BUNDLE_STORAGE)
            .subscribeOn(diskScheduler)
            .subscribe()
    }

    override fun retrieve(): List<TabInitializer> =
        FileUtils.readBundleFromStorage(application, BUNDLE_STORAGE)?.let { bundle ->
            bundle.keySet()
                .filter { it.startsWith(BUNDLE_KEY) }
                .mapNotNull { bundleKey ->
                    bundle.getBundle(bundleKey)?.let {
                        Pair(
                            it,
                            bundle.getString(TAB_TITLE_KEY + bundleKey.extractNumberFromEnd())
                        )
                    }
                }
        }?.map { (bundle, title) ->
            return@map bundle.getString(URL_KEY)?.let { url ->
                when {
                    url.isBookmarkUrl() -> bookmarkPageInitializer
                    url.isDownloadsUrl() -> downloadPageInitializer
                    url.isStartPageUrl() -> homePageInitializer
                    url.isHistoryUrl() -> historyPageInitializer
                    else -> homePageInitializer
                }
            } ?: FreezableBundleInitializer(
                bundle, title ?: application.getString(R.string.tab_frozen)
            )
        } ?: emptyList()

    override fun deleteAll() {
        FileUtils.deleteBundleInStorage(application, BUNDLE_STORAGE)
    }

    private fun String.extractNumberFromEnd(): String {
        val underScore = lastIndexOf('_')
        return if (underScore in 0 until length) {
            substring(underScore + 1)
        } else {
            ""
        }
    }

    companion object {
        private const val BUNDLE_KEY = "WEBVIEW_"
        private const val TAB_TITLE_KEY = "TITLE_"
        private const val URL_KEY = "URL_KEY"
        private const val BUNDLE_STORAGE = "SAVED_TABS.parcel"
    }
}
