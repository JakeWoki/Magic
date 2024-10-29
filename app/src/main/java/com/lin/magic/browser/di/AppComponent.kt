package com.lin.magic.browser.di

import com.lin.magic.BrowserApp
import com.lin.magic.ThemableBrowserActivity
import com.lin.magic.adblock.BloomFilterAdBlocker
import com.lin.magic.adblock.NoOpAdBlocker
import com.lin.magic.browser.search.SearchBoxModel
import com.lin.magic.device.BuildInfo
import com.lin.magic.dialog.LightningDialogBuilder
import com.lin.magic.search.SuggestionsAdapter
import com.lin.magic.settings.activity.ThemableSettingsActivity
import com.lin.magic.settings.fragment.AdBlockSettingsFragment
import com.lin.magic.settings.fragment.AdvancedSettingsFragment
import com.lin.magic.settings.fragment.BookmarkSettingsFragment
import com.lin.magic.settings.fragment.DebugSettingsFragment
import com.lin.magic.settings.fragment.DisplaySettingsFragment
import com.lin.magic.settings.fragment.GeneralSettingsFragment
import com.lin.magic.settings.fragment.PrivacySettingsFragment
import com.lin.magic.settings.fragment.RootSettingsFragment
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, AppBindsModule::class, Submodules::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun buildInfo(buildInfo: BuildInfo): Builder

        fun build(): AppComponent
    }

    fun inject(fragment: BookmarkSettingsFragment)

    fun inject(builder: LightningDialogBuilder)

    fun inject(activity: ThemableBrowserActivity)

    fun inject(advancedSettingsFragment: AdvancedSettingsFragment)

    fun inject(app: BrowserApp)

    fun inject(activity: ThemableSettingsActivity)

    fun inject(fragment: PrivacySettingsFragment)

    fun inject(fragment: DebugSettingsFragment)

    fun inject(suggestionsAdapter: SuggestionsAdapter)

    fun inject(searchBoxModel: SearchBoxModel)

    fun inject(activity: RootSettingsFragment)

    fun inject(generalSettingsFragment: GeneralSettingsFragment)

    fun inject(displaySettingsFragment: DisplaySettingsFragment)

    fun inject(adBlockSettingsFragment: AdBlockSettingsFragment)

    fun provideBloomFilterAdBlocker(): BloomFilterAdBlocker

    fun provideNoOpAdBlocker(): NoOpAdBlocker

    fun browser2ComponentBuilder(): Browser2Component.Builder

}

@Module(subcomponents = [Browser2Component::class])
internal class Submodules
