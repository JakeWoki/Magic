package com.lin.magic.browser.tab.bundle

import com.lin.magic.browser.tab.TabModel
import com.lin.magic.browser.tab.TabInitializer

/**
 * Used to save tab data for future restoration when the browser goes into hibernation.
 */
interface BundleStore {

    /**
     * Save the tab data for the list of [tabs].
     */
    fun save(tabs: List<TabModel>)

    /**
     * Synchronously previously stored tab data.
     */
    fun retrieve(): List<TabInitializer>

    /**
     * Synchronously delete all stored tabs.
     */
    fun deleteAll()
}
