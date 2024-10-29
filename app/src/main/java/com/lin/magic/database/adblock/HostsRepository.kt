package com.lin.magic.database.adblock

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * A repository that stores [Host].
 */
interface HostsRepository {

    /**
     * Add the [List] of [Host] to the repository.
     *
     * @return A [Completable] that completes when the addition finishes.
     */
    fun addHosts(hosts: List<Host>): Completable

    /**
     * Remove all hosts in the repository.
     *
     * @return A [Completable] that completes when the removal finishes.
     */
    fun removeAllHosts(): Completable

    /**
     * @return `true` if the repository contains the [Host], `false` otherwise.
     */
    fun containsHost(host: Host): Boolean

    /**
     * @return `true` if the repository has been initialized, `false` otherwise.
     */
    fun hasHosts(): Boolean

    /**
     * @return A [Single] that emits a list of all hosts in the repository.
     */
    fun allHosts(): Single<List<Host>>

}
