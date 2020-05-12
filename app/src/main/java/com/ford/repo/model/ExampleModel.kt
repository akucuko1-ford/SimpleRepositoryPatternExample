package com.ford.repo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "myTable")
data class ExampleModel(
    @PrimaryKey
    val vin: String,
    val jsonData: String,
    val time: Long
)