package com.lin.magic.browser.di

import com.lin.magic.browser.BrowserContract
import com.lin.magic.browser.BrowserNavigator
import com.lin.magic.browser.cleanup.DelegatingExitCleanup
import com.lin.magic.browser.cleanup.ExitCleanup
import com.lin.magic.browser.image.FaviconImageLoader
import com.lin.magic.browser.image.ImageLoader
import com.lin.magic.browser.tab.TabsRepository
import com.lin.magic.browser.theme.DefaultThemeProvider
import com.lin.magic.browser.theme.ThemeProvider
import android.app.Activity
import androidx.fragment.app.FragmentActivity
import dagger.Binds
import dagger.Module

/**
 * Binds implementations to interfaces for the browser scope.
 */
@Module
interface Browser2BindsModule {

    @Binds
    fun bindsActivity(fragmentActivity: FragmentActivity): Activity

    @Binds
    fun bindsBrowserModel(tabsRepository: TabsRepository): BrowserContract.Model

    @Binds
    fun bindsFaviconImageLoader(faviconImageLoader: FaviconImageLoader): ImageLoader

    @Binds
    fun bindsBrowserNavigator(browserNavigator: BrowserNavigator): BrowserContract.Navigator

    @Binds
    fun bindsExitCleanup(delegatingExitCleanup: DelegatingExitCleanup): ExitCleanup

    @Binds
    fun bindsThemeProvider(legacyThemeProvider: DefaultThemeProvider): ThemeProvider
}
