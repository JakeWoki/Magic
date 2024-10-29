package com.lin.magic.browser.di

import com.lin.magic.R
import com.lin.magic.browser.tab.DefaultTabTitle
import com.lin.magic.device.BuildInfo
import com.lin.magic.device.BuildType
import com.lin.magic.html.ListPageReader
import com.lin.magic.html.bookmark.BookmarkPageReader
import com.lin.magic.html.homepage.HomePageReader
import com.lin.magic.js.InvertPage
import com.lin.magic.js.TextReflow
import com.lin.magic.js.ThemeColor
import com.lin.magic.log.AndroidLogger
import com.lin.magic.log.Logger
import com.lin.magic.log.NoOpLogger
import com.lin.magic.search.suggestions.RequestFactory
import com.lin.magic.utils.FileUtils
import android.app.Application
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ShortcutManager
import android.content.res.AssetManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.anthonycr.mezzanine.MezzanineGenerator
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @MainHandler
    fun provideMainHandler() = Handler(Looper.getMainLooper())

    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @UserPrefs
    fun provideUserPreferences(application: Application): SharedPreferences =
        application.getSharedPreferences("settings", 0)

    @Provides
    @DevPrefs
    fun provideDebugPreferences(application: Application): SharedPreferences =
        application.getSharedPreferences("developer_settings", 0)

    @Provides
    @AdBlockPrefs
    fun provideAdBlockPreferences(application: Application): SharedPreferences =
        application.getSharedPreferences("ad_block_settings", 0)

    @Provides
    fun providesAssetManager(application: Application): AssetManager = application.assets

    @Provides
    fun providesClipboardManager(application: Application) =
        application.getSystemService<ClipboardManager>()!!

    @Provides
    fun providesInputMethodManager(application: Application) =
        application.getSystemService<InputMethodManager>()!!

    @Provides
    fun providesDownloadManager(application: Application) =
        application.getSystemService<DownloadManager>()!!

    @Provides
    fun providesConnectivityManager(application: Application) =
        application.getSystemService<ConnectivityManager>()!!

    @Provides
    fun providesNotificationManager(application: Application) =
        application.getSystemService<NotificationManager>()!!

    @Provides
    fun providesWindowManager(application: Application) =
        application.getSystemService<WindowManager>()!!

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    @Provides
    fun providesShortcutManager(application: Application) =
        application.getSystemService<ShortcutManager>()!!

    @Provides
    @DatabaseScheduler
    @Singleton
    fun providesIoThread(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    @Provides
    @DiskScheduler
    @Singleton
    fun providesDiskThread(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    @Provides
    @NetworkScheduler
    @Singleton
    fun providesNetworkThread(): Scheduler =
        Schedulers.from(ThreadPoolExecutor(0, 4, 60, TimeUnit.SECONDS, LinkedBlockingDeque()))

    @Provides
    @MainScheduler
    @Singleton
    fun providesMainThread(): Scheduler = AndroidSchedulers.mainThread()

    @Singleton
    @Provides
    fun providesSuggestionsCacheControl(): CacheControl =
        CacheControl.Builder().maxStale(1, TimeUnit.DAYS).build()

    @Singleton
    @Provides
    fun providesSuggestionsRequestFactory(cacheControl: CacheControl): RequestFactory =
        object : RequestFactory {
            override fun createSuggestionsRequest(httpUrl: HttpUrl, encoding: String): Request {
                return Request.Builder().url(httpUrl)
                    .addHeader("Accept-Charset", encoding)
                    .cacheControl(cacheControl)
                    .build()
            }
        }

    private fun createInterceptorWithMaxCacheAge(maxCacheAgeSeconds: Long) = Interceptor { chain ->
        chain.proceed(chain.request()).newBuilder()
            .header("cache-control", "max-age=$maxCacheAgeSeconds, max-stale=$maxCacheAgeSeconds")
            .build()
    }

    @Singleton
    @Provides
    @SuggestionsClient
    fun providesSuggestionsHttpClient(application: Application): Single<OkHttpClient> =
        Single.fromCallable {
            val intervalDay = TimeUnit.DAYS.toSeconds(1)
            val suggestionsCache = File(application.cacheDir, "suggestion_responses")

            return@fromCallable OkHttpClient.Builder()
                .cache(Cache(suggestionsCache, FileUtils.megabytesToBytes(1)))
                .addNetworkInterceptor(createInterceptorWithMaxCacheAge(intervalDay))
                .build()
        }.cache()

    @Singleton
    @Provides
    @HostsClient
    fun providesHostsHttpClient(application: Application): Single<OkHttpClient> =
        Single.fromCallable {
            val intervalYear = TimeUnit.DAYS.toSeconds(365)
            val suggestionsCache = File(application.cacheDir, "hosts_cache")

            return@fromCallable OkHttpClient.Builder()
                .cache(Cache(suggestionsCache, FileUtils.megabytesToBytes(5)))
                .addNetworkInterceptor(createInterceptorWithMaxCacheAge(intervalYear))
                .build()
        }.cache()

    @Provides
    @Singleton
    fun provideLogger(buildInfo: BuildInfo): Logger = if (buildInfo.buildType == BuildType.DEBUG) {
        AndroidLogger()
    } else {
        NoOpLogger()
    }

    @Provides
    fun providesListPageReader(): ListPageReader = MezzanineGenerator.ListPageReader()

    @Provides
    fun providesHomePageReader(): HomePageReader = MezzanineGenerator.HomePageReader()

    @Provides
    fun providesBookmarkPageReader(): BookmarkPageReader = MezzanineGenerator.BookmarkPageReader()

    @Provides
    fun providesTextReflow(): TextReflow = MezzanineGenerator.TextReflow()

    @Provides
    fun providesThemeColor(): ThemeColor = MezzanineGenerator.ThemeColor()

    @Provides
    fun providesInvertPage(): InvertPage = MezzanineGenerator.InvertPage()

    @DefaultTabTitle
    @Provides
    fun providesDefaultTabTitle(application: Application): String =
        application.getString(R.string.untitled)

}

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class SuggestionsClient

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class HostsClient

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class MainHandler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class UserPrefs

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class AdBlockPrefs

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class DevPrefs

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class MainScheduler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class DiskScheduler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class NetworkScheduler

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class DatabaseScheduler
