package com.lin.magic.utils

import com.lin.magic.preference.DeveloperPreferences
import leakcanary.LeakCanary
import javax.inject.Inject

/**
 * Sets up LeakCanary.
 */
class LeakCanaryUtils @Inject constructor(private val developerPreferences: DeveloperPreferences) {

    /**
     * Setup LeakCanary
     */
    fun setup() {
        LeakCanary.config = LeakCanary.config.copy(
            dumpHeap = developerPreferences.useLeakCanary
        )
    }

}
