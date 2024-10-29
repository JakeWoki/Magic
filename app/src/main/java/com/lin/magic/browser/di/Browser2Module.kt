package com.lin.magic.browser.di

import com.lin.magic.R
import com.lin.magic.browser.BrowserContract
import com.lin.magic.browser.data.CookieAdministrator
import com.lin.magic.browser.data.DefaultCookieAdministrator
import com.lin.magic.browser.history.DefaultHistoryRecord
import com.lin.magic.browser.history.HistoryRecord
import com.lin.magic.browser.history.NoOpHistoryRecord
import com.lin.magic.browser.image.IconFreeze
import com.lin.magic.browser.notification.DefaultTabCountNotifier
import com.lin.magic.browser.notification.IncognitoTabCountNotifier
import com.lin.magic.browser.notification.TabCountNotifier
import com.lin.magic.browser.search.IntentExtractor
import com.lin.magic.browser.tab.DefaultUserAgent
import com.lin.magic.browser.tab.bundle.BundleStore
import com.lin.magic.browser.tab.bundle.DefaultBundleStore
import com.lin.magic.browser.tab.bundle.IncognitoBundleStore
import com.lin.magic.browser.ui.BookmarkConfiguration
import com.lin.magic.browser.ui.TabConfiguration
import com.lin.magic.browser.ui.UiConfiguration
import com.lin.magic.adblock.AdBlocker
import com.lin.magic.adblock.BloomFilterAdBlocker
import com.lin.magic.adblock.NoOpAdBlocker
import com.lin.magic.extensions.drawable
import com.lin.magic.preference.UserPreferences
import com.lin.magic.utils.IntentUtils
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.webkit.WebSettings
import androidx.core.graphics.drawable.toBitmap
import dagger.Module
import dagger.Provides
import javax.inject.Provider

/**
 * Constructs dependencies for the browser scope.
 */
@Module
class Browser2Module {

    @Provides
    fun providesAdBlocker(
        userPreferences: UserPreferences,
        bloomFilterAdBlocker: Provider<BloomFilterAdBlocker>,
        noOpAdBlocker: NoOpAdBlocker
    ): AdBlocker = if (userPreferences.adBlockEnabled) {
        bloomFilterAdBlocker.get()
    } else {
        noOpAdBlocker
    }

    // TODO: dont force cast
    @Provides
    @InitialUrl
    fun providesInitialUrl(
        @InitialIntent initialIntent: Intent,
        intentExtractor: IntentExtractor
    ): String? =
        (intentExtractor.extractUrlFromIntent(initialIntent) as? BrowserContract.Action.LoadUrl)?.url

    // TODO: auto inject intent utils
    @Provides
    fun providesIntentUtils(activity: Activity): IntentUtils = IntentUtils(activity)

    @Provides
    fun providesUiConfiguration(
        userPreferences: UserPreferences
    ): UiConfiguration = UiConfiguration(
        tabConfiguration = if (userPreferences.showTabsInDrawer) {
            TabConfiguration.DRAWER
        } else {
            TabConfiguration.DESKTOP
        },
        bookmarkConfiguration = if (userPreferences.bookmarksAndTabsSwapped) {
            BookmarkConfiguration.LEFT
        } else {
            BookmarkConfiguration.RIGHT
        }
    )

    @DefaultUserAgent
    @Provides
    fun providesDefaultUserAgent(application: Application): String =
        WebSettings.getDefaultUserAgent(application)


    @Provides
    fun providesHistoryRecord(
        @IncognitoMode incognitoMode: Boolean,
        defaultHistoryRecord: DefaultHistoryRecord
    ): HistoryRecord = if (incognitoMode) {
        NoOpHistoryRecord
    } else {
        defaultHistoryRecord
    }

    @Provides
    fun providesCookieAdministrator(
        @IncognitoMode incognitoMode: Boolean,
        defaultCookieAdministrator: DefaultCookieAdministrator,
        incognitoCookieAdministrator: DefaultCookieAdministrator
    ): CookieAdministrator = if (incognitoMode) {
        incognitoCookieAdministrator
    } else {
        defaultCookieAdministrator
    }

    @Provides
    fun providesTabCountNotifier(
        @IncognitoMode incognitoMode: Boolean,
        incognitoTabCountNotifier: IncognitoTabCountNotifier
    ): TabCountNotifier = if (incognitoMode) {
        incognitoTabCountNotifier
    } else {
        DefaultTabCountNotifier
    }

    @Provides
    fun providesBundleStore(
        @IncognitoMode incognitoMode: Boolean,
        defaultBundleStore: DefaultBundleStore
    ): BundleStore = if (incognitoMode) {
        IncognitoBundleStore
    } else {
        defaultBundleStore
    }

    @IconFreeze
    @Provides
    fun providesFrozenIcon(activity: Activity): Bitmap =
        activity.drawable(R.drawable.ic_frozen).toBitmap()

}
