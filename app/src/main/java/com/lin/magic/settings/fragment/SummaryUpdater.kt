package com.lin.magic.settings.fragment

import androidx.preference.Preference

/**
 * A command that updates the summary of a preference.
 */
class SummaryUpdater(private val preference: Preference) {

    /**
     * Updates the summary of the preference.
     *
     * @param text the text to display in the summary.
     */
    fun updateSummary(text: String) {
        preference.summary = text
    }

}
