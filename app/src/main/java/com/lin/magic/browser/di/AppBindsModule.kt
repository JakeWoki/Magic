package com.lin.magic.browser.di

import com.lin.magic.adblock.allowlist.AllowListModel
import com.lin.magic.adblock.allowlist.SessionAllowListModel
import com.lin.magic.adblock.source.AssetsHostsDataSource
import com.lin.magic.adblock.source.HostsDataSource
import com.lin.magic.adblock.source.HostsDataSourceProvider
import com.lin.magic.adblock.source.PreferencesHostsDataSourceProvider
import com.lin.magic.database.adblock.HostsDatabase
import com.lin.magic.database.adblock.HostsRepository
import com.lin.magic.database.allowlist.AdBlockAllowListDatabase
import com.lin.magic.database.allowlist.AdBlockAllowListRepository
import com.lin.magic.database.bookmark.BookmarkDatabase
import com.lin.magic.database.bookmark.BookmarkRepository
import com.lin.magic.database.downloads.DownloadsDatabase
import com.lin.magic.database.downloads.DownloadsRepository
import com.lin.magic.database.history.HistoryDatabase
import com.lin.magic.database.history.HistoryRepository
import com.lin.magic.ssl.SessionSslWarningPreferences
import com.lin.magic.ssl.SslWarningPreferences
import dagger.Binds
import dagger.Module

/**
 * Dependency injection module used to bind implementations to interfaces.
 */
@Module
interface AppBindsModule {

    @Binds
    fun bindsBookmarkModel(bookmarkDatabase: BookmarkDatabase): BookmarkRepository

    @Binds
    fun bindsDownloadsModel(downloadsDatabase: DownloadsDatabase): DownloadsRepository

    @Binds
    fun bindsHistoryModel(historyDatabase: HistoryDatabase): HistoryRepository

    @Binds
    fun bindsAdBlockAllowListModel(adBlockAllowListDatabase: AdBlockAllowListDatabase): AdBlockAllowListRepository

    @Binds
    fun bindsAllowListModel(sessionAllowListModel: SessionAllowListModel): AllowListModel

    @Binds
    fun bindsSslWarningPreferences(sessionSslWarningPreferences: SessionSslWarningPreferences): SslWarningPreferences

    @Binds
    fun bindsHostsDataSource(assetsHostsDataSource: AssetsHostsDataSource): HostsDataSource

    @Binds
    fun bindsHostsRepository(hostsDatabase: HostsDatabase): HostsRepository

    @Binds
    fun bindsHostsDataSourceProvider(preferencesHostsDataSourceProvider: PreferencesHostsDataSourceProvider): HostsDataSourceProvider
}
