package com.example.aiassistant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import leakcanary.LeakCanary

@HiltAndroidApp
class MyApplication : Application()