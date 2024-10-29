package com.lin.magic.browser.notification

/**
 * Do nothing when notified about the new tab count.
 */
object DefaultTabCountNotifier : TabCountNotifier {
    override fun notifyTabCountChange(total: Int) = Unit
}
