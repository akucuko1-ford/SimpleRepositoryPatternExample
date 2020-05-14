package com.ford.repo

import android.util.Log
import com.ford.repo.api.ExampleApi
import com.ford.repo.db.ExampleDbService
import com.ford.repo.model.ExampleModel
import com.ford.smartrepo.SmartRepository
import io.reactivex.Flowable
import io.reactivex.Observable

class ExampleApiAdapter(
    private val apiService: ExampleApi,
    private val dbService: ExampleDbService
) : SmartRepository.Adapter<ExampleModel> {

    override fun onCreateModelInstance(vin: String): ExampleModel =
        ExampleModel(vin, "", -1)

    override fun getDatabaseData(vin: String): Flowable<List<ExampleModel>> =
        dbService.getDatabaseData(vin)
            .doOnEach { Log.i("ExampleApiAdapter", "Data has been retrieved from DB, value=$it") }

    override fun isCacheDataInvalid(data: ExampleModel): Boolean {
        val currentTime = System.currentTimeMillis()
        val endTime = data.time + 60000
        val timeDiff = currentTime - endTime
        Log.i("ExampleApiAdapter", "time diff = $timeDiff")
        return timeDiff > 0
    }

    override fun getNetworkData(vin: String): Observable<ExampleModel> =
        apiService.makeNetworkCall(vin)

    override fun saveToDatabase(newData: ExampleModel, cachedData: ExampleModel) =
        dbService.insertData(newData)
}