package com.ford.smartrepo

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class SmartRepository<T>(private val adapter: Adapter<T>) {

    fun getObservable(vin: String): Observable<T> = getDbObservable(vin)
        .subscribeOn(Schedulers.io())
        .map { getData(it, vin) }
        .observeOn(Schedulers.single())
        .flatMap { resumeApiCall(vin, it).startWith(it).toFlowable(BackpressureStrategy.BUFFER) }
        .observeOn(Schedulers.computation())
        .toObservable()
        .distinctUntilChanged()

    private fun getDbObservable(vin: String) =
        adapter.memoryObservablesMap()[vin] ?: adapter.getDatabaseData(vin).replay(1).refCount()
            .apply { adapter.memoryObservablesMap()[vin] = this }

    private fun getData(it: List<T>, vin: String) =
        if (it.isEmpty()) adapter.onCreateModelInstance(vin)
        else it[0]

    private fun resumeApiCall(vin: String, cacheValue: T): Observable<T> =
        if (adapter.isCacheDataInvalid(cacheValue))
            adapter.networkObservablesMap()[vin] ?: networkApiCall(vin, cacheValue)
        else Observable.empty()

    private fun networkApiCall(vin: String, cacheValue: T): Observable<T> =
        adapter.getNetworkData(vin)
            .subscribeOn(Schedulers.io())
            .doOnNext { adapter.saveToDatabase(it, cacheValue) }
            .onErrorResumeNext { _: Throwable -> Observable.empty() }
            .doFinally { adapter.networkObservablesMap().remove(vin) }
            .apply { adapter.networkObservablesMap()[vin] = this }

    interface Adapter<VH> {
        fun onCreateModelInstance(vin: String): VH
        fun getDatabaseData(vin: String): Flowable<List<VH>>
        fun isCacheDataInvalid(data: VH): Boolean
        fun getNetworkData(vin: String): Observable<VH>
        fun saveToDatabase(newData: VH, cachedData: VH)
        fun networkObservablesMap(): MutableMap<String, Observable<VH>>
        fun memoryObservablesMap(): MutableMap<String, Flowable<List<VH>>>
    }
}