package com.octanews.infoin // Pastikan package name ini sesuai dengan proyekmu

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Baris ini yang paling penting: menginisialisasi Firebase
        FirebaseApp.initializeApp(this)
    }
}