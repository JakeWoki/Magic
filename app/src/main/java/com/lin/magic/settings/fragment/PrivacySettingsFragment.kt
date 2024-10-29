package com.lin.magic.settings.fragment

import com.lin.magic.R
import com.lin.magic.browser.di.DatabaseScheduler
import com.lin.magic.browser.di.MainScheduler
import com.lin.magic.browser.di.injector
import com.lin.magic.browser.tab.WebViewFactory
import com.lin.magic.database.history.HistoryRepository
import com.lin.magic.dialog.BrowserDialog
import com.lin.magic.dialog.DialogItem
import com.lin.magic.extensions.snackbar
import com.lin.magic.preference.UserPreferences
import com.lin.magic.utils.WebUtils
import android.os.Bundle
import android.webkit.WebView
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Inject

class PrivacySettingsFragment : AbstractSettingsFragment() {

    @Inject internal lateinit var historyRepository: HistoryRepository
    @Inject internal lateinit var userPreferences: UserPreferences
    @Inject @field:DatabaseScheduler
    internal lateinit var databaseScheduler: Scheduler
    @Inject @field:MainScheduler
    internal lateinit var mainScheduler: Scheduler

    override fun providePreferencesXmlResource() = R.xml.preference_privacy

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        injector.inject(this)

        clickablePreference(preference = SETTINGS_CLEARCACHE, onClick = this::clearCache)
        clickablePreference(preference = SETTINGS_CLEARHISTORY, onClick = this::clearHistoryDialog)
        clickablePreference(preference = SETTINGS_CLEARCOOKIES, onClick = this::clearCookiesDialog)
        clickablePreference(preference = SETTINGS_CLEARWEBSTORAGE, onClick = this::clearWebStorage)

        togglePreference(
            preference = SETTINGS_LOCATION,
            isChecked = userPreferences.locationEnabled,
            onCheckChange = { userPreferences.locationEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_THIRDPCOOKIES,
            isChecked = userPreferences.blockThirdPartyCookiesEnabled,
            onCheckChange = { userPreferences.blockThirdPartyCookiesEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_SAVEPASSWORD,
            isChecked = userPreferences.savePasswordsEnabled,
            onCheckChange = { userPreferences.savePasswordsEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_CACHEEXIT,
            isChecked = userPreferences.clearCacheExit,
            onCheckChange = { userPreferences.clearCacheExit = it }
        )

        togglePreference(
            preference = SETTINGS_HISTORYEXIT,
            isChecked = userPreferences.clearHistoryExitEnabled,
            onCheckChange = { userPreferences.clearHistoryExitEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_COOKIEEXIT,
            isChecked = userPreferences.clearCookiesExitEnabled,
            onCheckChange = { userPreferences.clearCookiesExitEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_WEBSTORAGEEXIT,
            isChecked = userPreferences.clearWebStorageExitEnabled,
            onCheckChange = { userPreferences.clearWebStorageExitEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_DONOTTRACK,
            isChecked = userPreferences.doNotTrackEnabled,
            onCheckChange = { userPreferences.doNotTrackEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_WEBRTC,
            isChecked = userPreferences.webRtcEnabled,
            onCheckChange = { userPreferences.webRtcEnabled = it }
        )

        togglePreference(
            preference = SETTINGS_IDENTIFYINGHEADERS,
            isChecked = userPreferences.removeIdentifyingHeadersEnabled,
            summary = "${WebViewFactory.HEADER_REQUESTED_WITH}, ${WebViewFactory.HEADER_WAP_PROFILE}",
            onCheckChange = { userPreferences.removeIdentifyingHeadersEnabled = it }
        )

    }

    private fun clearHistoryDialog() {
        BrowserDialog.showPositiveNegativeDialog(
            activity = requireActivity(),
            title = R.string.title_clear_history,
            message = R.string.dialog_history,
            positiveButton = DialogItem(title = R.string.action_yes) {
                clearHistory()
                    .subscribeOn(databaseScheduler)
                    .observeOn(mainScheduler)
                    .subscribe {
                        activity?.snackbar(R.string.message_clear_history)
                    }
            },
            negativeButton = DialogItem(title = R.string.action_no) {},
            onCancel = {}
        )
    }

    private fun clearCookiesDialog() {
        BrowserDialog.showPositiveNegativeDialog(
            activity = requireActivity(),
            title = R.string.title_clear_cookies,
            message = R.string.dialog_cookies,
            positiveButton = DialogItem(title = R.string.action_yes) {
                clearCookies()
                    .subscribeOn(databaseScheduler)
                    .observeOn(mainScheduler)
                    .subscribe {
                        activity?.snackbar(R.string.message_cookies_cleared)
                    }
            },
            negativeButton = DialogItem(title = R.string.action_no) {},
            onCancel = {}
        )
    }

    private fun clearCache() {
        WebView(requireNotNull(activity)).apply {
            clearCache(true)
            destroy()
        }
        activity?.snackbar(R.string.message_cache_cleared)
    }

    private fun clearHistory(): Completable = Completable.fromAction {
        val activity = activity
        if (activity != null) {
            // TODO: 6/9/17 clearHistory is not synchronous
            WebUtils.clearHistory(activity, historyRepository, databaseScheduler)
        } else {
            throw RuntimeException("Activity was null in clearHistory")
        }
    }

    private fun clearCookies(): Completable = Completable.fromAction {
        WebUtils.clearCookies()
    }

    private fun clearWebStorage() {
        WebUtils.clearWebStorage()
        activity?.snackbar(R.string.message_web_storage_cleared)
    }

    companion object {
        private const val SETTINGS_LOCATION = "location"
        private const val SETTINGS_THIRDPCOOKIES = "third_party"
        private const val SETTINGS_SAVEPASSWORD = "password"
        private const val SETTINGS_CACHEEXIT = "clear_cache_exit"
        private const val SETTINGS_HISTORYEXIT = "clear_history_exit"
        private const val SETTINGS_COOKIEEXIT = "clear_cookies_exit"
        private const val SETTINGS_CLEARCACHE = "clear_cache"
        private const val SETTINGS_CLEARHISTORY = "clear_history"
        private const val SETTINGS_CLEARCOOKIES = "clear_cookies"
        private const val SETTINGS_CLEARWEBSTORAGE = "clear_webstorage"
        private const val SETTINGS_WEBSTORAGEEXIT = "clear_webstorage_exit"
        private const val SETTINGS_DONOTTRACK = "do_not_track"
        private const val SETTINGS_WEBRTC = "webrtc_support"
        private const val SETTINGS_IDENTIFYINGHEADERS = "remove_identifying_headers"
    }

}
