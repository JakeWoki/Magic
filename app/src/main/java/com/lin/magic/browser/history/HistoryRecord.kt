package com.lin.magic.browser.history

/**
 * Records browser history.
 */
interface HistoryRecord {

    /**
     * Record a visit to the [url] with the provided [title].
     */
    fun visit(title: String, url: String)

}
