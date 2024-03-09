package ru.netology.nework.app.application

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nework.BuildConfig
import ru.netology.nework.app.auth.AppAuth
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var auth: AppAuth

    private val appScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        setupAuth()
        MapKitFactory.setApiKey(BuildConfig.MAPS_API_KEY)
    }

    private fun setupAuth() {
        appScope.launch {
            auth.sendPushToken()
          
        }
    }
}