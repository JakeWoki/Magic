package com.lin.magic.browser.history

/**
 * A non functional history record that ignores all attempts to record a visit.
 */
object NoOpHistoryRecord : HistoryRecord {
    override fun visit(title: String, url: String) = Unit
}
