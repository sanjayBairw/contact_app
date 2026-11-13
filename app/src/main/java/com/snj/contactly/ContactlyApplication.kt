package com.snj.contactly

import android.app.Application
import com.snj.contactly.data.local.ContactDatabase
import com.snj.contactly.data.repository.ContactRepository

class ContactlyApplication : Application() {

    val database: ContactDatabase by lazy {
        ContactDatabase.getInstance(this)
    }

    val repository: ContactRepository by lazy {
        ContactRepository(database.contactDao())
    }
}

