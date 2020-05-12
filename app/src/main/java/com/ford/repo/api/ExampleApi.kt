package com.ford.repo.api

import android.util.Log
import com.ford.repo.model.ExampleModel
import io.reactivex.Observable

class ExampleApi {

    fun makeNetworkCall(vin: String): Observable<ExampleModel> =
        Observable.just(ExampleModel(vin, data[counter++], System.currentTimeMillis()))
            .doOnEach { Log.i("ExampleApi", "Network call has been made, value=$it") }
}

private var counter = 0
private val data = listOf(
    "JsonData1",
    "JsonData2",
    "JsonData3",
    "JsonData4",
    "JsonData5",
    "JsonData6",
    "JsonData7",
    "JsonData8",
    "JsonData9",
    "JsonData0"
)