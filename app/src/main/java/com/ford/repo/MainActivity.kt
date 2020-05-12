package com.ford.repo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.ford.repo.api.ExampleApi
import com.ford.repo.db.ExampleDatabase
import com.ford.smartrepo.SmartRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var sub1: Disposable? = null
    private var sub2: Disposable? = null
    private var sub3: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = Room.databaseBuilder(this, ExampleDatabase::class.java, "my_database").build()
        val api = ExampleApi()
        val smartRepo = SmartRepository(ExampleApiAdapter(api, db.dao()))

        //First Subscriber
        firstSbButton.setOnClickListener {
            sub1?.dispose()
            sub1 = smartRepo.getObservable("vin")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    firstSbText.text = it.jsonData
                    Log.i("ExampleActivity", "first subscriber has been updated, value=$it")
                }
        }

        //Second Subscriber
        secondSbButton.setOnClickListener {
            sub2?.dispose()
            sub2 = smartRepo.getObservable("vin")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    secondSbText.text = it.jsonData
                    Log.i("ExampleActivity", "second subscriber has been updated, value=$it")
                }
        }

        //Third Subscriber
        thirdSbButton.setOnClickListener {
            sub3?.dispose()
            sub3 = smartRepo.getObservable("vin")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    thirdSbText.text = it.jsonData
                    Log.i("ExampleActivity", "third subscriber has been updated, value=$it")
                }
        }
    }
}
