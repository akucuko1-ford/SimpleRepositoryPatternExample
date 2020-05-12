package com.ford.repo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ford.repo.model.ExampleModel
import io.reactivex.Flowable

@Dao
interface ExampleDbService {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(model: ExampleModel)

    @Query("select * from myTable where vin = :vin")
    fun getDatabaseData(vin: String): Flowable<List<ExampleModel>>
}