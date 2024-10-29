package com.lin.magic.database.adblock

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An in memory hosts repository. Hosts are stored in a [Set].
 */
@Singleton
class InMemoryHostsRepository @Inject constructor() : HostsRepository {

    private var mutableHostsSet: Set<Host> = emptySet()

    override fun addHosts(hosts: List<Host>): Completable = Completable.fromAction {
        mutableHostsSet = hosts.toSet()
    }

    override fun removeAllHosts(): Completable = Completable.fromAction {
        mutableHostsSet = emptySet()
    }

    override fun containsHost(host: Host): Boolean = mutableHostsSet.contains(host)

    override fun hasHosts(): Boolean = mutableHostsSet.isNotEmpty()

    override fun allHosts(): Single<List<Host>> = Single.just(mutableHostsSet.toList())
}
