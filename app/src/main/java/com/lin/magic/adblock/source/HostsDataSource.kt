package com.lin.magic.adblock.source

import io.reactivex.rxjava3.core.Single

/**
 * A data source that contains hosts.
 */
interface HostsDataSource {

    /**
     * Load the hosts and emit them as a [Single] [HostsResult].
     */
    fun loadHosts(): Single<HostsResult>

    /**
     * The unique [String] identifier for this source.
     */
    fun identifier(): String

}
