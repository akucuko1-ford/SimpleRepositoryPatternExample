package com.ford.repo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ford.repo.model.ExampleModel

@Database(entities = [ExampleModel::class], version = 1)
abstract class ExampleDatabase : RoomDatabase() {
    abstract fun dao(): ExampleDbService
}