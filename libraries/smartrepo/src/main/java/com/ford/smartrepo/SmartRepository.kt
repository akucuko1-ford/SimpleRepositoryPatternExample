package com.ford.smartrepo

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class SmartRepository<T>(private val adapter: Adapter<T>) {

    private val networkMap = mutableMapOf<String, Observable<T>>()
    private val memoryMap = mutableMapOf<String, Flowable<List<T>>>()

    fun getObservable(vin: String): Observable<T> = getDbObservable(vin).toObservable()
        .subscribeOn(Schedulers.io())
        .map { getData(it, vin) }
        .observeOn(Schedulers.single())
        .flatMap { resumeApiCall(vin, it).startWith(it) }
        .observeOn(Schedulers.computation())
        .distinctUntilChanged()

    private fun getDbObservable(vin: String) =
        memoryMap[vin] ?: adapter.getDatabaseData(vin).replay(1).refCount()
            .apply { memoryMap[vin] = this }

    private fun getData(it: List<T>, vin: String) =
        if (it.isEmpty()) adapter.onCreateModelInstance(vin)
        else it[0]

    private fun resumeApiCall(vin: String, cacheValue: T): Observable<T> =
        if (adapter.isCacheDataInvalid(cacheValue))
            networkMap[vin] ?: networkApiCall(vin, cacheValue)
        else Observable.empty()

    private fun networkApiCall(vin: String, cacheValue: T): Observable<T> =
        adapter.getNetworkData(vin)
            .subscribeOn(Schedulers.io())
            .doOnNext { adapter.saveToDatabase(it, cacheValue) }
            .onErrorResumeNext { _: Throwable -> Observable.empty() }
            .doFinally { networkMap.remove(vin) }
            .share()
            .apply { networkMap[vin] = this }

    interface Adapter<VH> {

        /*
        * SmartRepository uses this function to create default model in case of database empty
        * */
        fun onCreateModelInstance(vin: String): VH

        /*
        * To retrieve data from database which created by the client
        * */
        fun getDatabaseData(vin: String): Flowable<List<VH>>

        /*
        * The client should decide whether the cache is valid or not
        * */
        fun isCacheDataInvalid(data: VH): Boolean

        /*
        * To retrieve data from api.
        *
        * This is necessary if the cache is invalid
        * */
        fun getNetworkData(vin: String): Observable<VH>

        /*
        * The client should handle the login for saving the data to database or etc.
        * */
        fun saveToDatabase(newData: VH, cachedData: VH)
    }
}