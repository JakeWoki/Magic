package com.lin.magic.browser.tab.bundle

import com.lin.magic.browser.tab.TabModel
import com.lin.magic.browser.tab.TabInitializer

/**
 * A bundle store implementation that no-ops for for incognito mode.
 */
object IncognitoBundleStore : BundleStore {
    override fun save(tabs: List<TabModel>) = Unit

    override fun retrieve(): List<TabInitializer> = emptyList()

    override fun deleteAll() = Unit
}
