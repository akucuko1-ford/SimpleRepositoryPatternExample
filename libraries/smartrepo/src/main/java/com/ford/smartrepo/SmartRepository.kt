package com.ford.smartrepo

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class SmartRepository<K, T>(private val adapter: Adapter<K, T>) {

    private val networkMap = mutableMapOf<K, Observable<T>>()
    private val memoryMap = mutableMapOf<K, Flowable<List<T>>>()

    fun getObservable(key: K): Observable<T> = getDbObservable(key).toObservable()
        .subscribeOn(Schedulers.io())
        .map { getData(it, key) }
        .observeOn(Schedulers.single())
        .flatMap { resumeApiCall(key, it).startWith(it) }
        .observeOn(Schedulers.computation())
        .distinctUntilChanged()

    private fun getDbObservable(key: K) =
        memoryMap[key] ?: adapter.getDatabaseData(key).replay(1).refCount()
            .apply { memoryMap[key] = this }

    private fun getData(items: List<T>, key: K) =
        if (items.isEmpty()) adapter.onCreateModelInstance(key)
        else items[0]

    private fun resumeApiCall(key: K, cacheValue: T): Observable<T> =
        if (adapter.isCacheDataInvalid(cacheValue))
            networkMap[key] ?: networkApiCall(key, cacheValue)
        else Observable.empty()

    private fun networkApiCall(key: K, cacheValue: T): Observable<T> =
        adapter.getNetworkData(key)
            .subscribeOn(Schedulers.io())
            .doOnNext { adapter.saveToDatabase(it, cacheValue) }
            .onErrorResumeNext { _: Throwable -> Observable.empty() }
            .doFinally { networkMap.remove(key) }
            .share()
            .apply { networkMap[key] = this }

    interface Adapter<KY, VH> {

        /*
        * SmartRepository uses this function to create default model in case of database empty
        * */
        fun onCreateModelInstance(key: KY): VH

        /*
        * To retrieve data from database which created by the client
        * */
        fun getDatabaseData(key: KY): Flowable<List<VH>>

        /*
        * The client should decide whether the cache is valid or not
        * */
        fun isCacheDataInvalid(data: VH): Boolean

        /*
        * To retrieve data from api.
        *
        * This is necessary if the cache is invalid
        * */
        fun getNetworkData(key: KY): Observable<VH>

        /*
        * The client should handle the login for saving the data to database or etc.
        * */
        fun saveToDatabase(newData: VH, oldData: VH)
    }
}