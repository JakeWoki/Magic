package com.lin.magic.browser.ui

/**
 * The configuration of the adjustable UI elements.
 *
 * @param tabConfiguration The configuration of the tabs.
 * @param bookmarkConfiguration The configuration of the bookmarks.
 */
data class UiConfiguration(
    val tabConfiguration: TabConfiguration,
    val bookmarkConfiguration: BookmarkConfiguration
)
