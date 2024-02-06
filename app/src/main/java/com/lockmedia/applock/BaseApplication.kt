
package com.lockmedia.applock
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.*
import com.google.android.gms.ads.MobileAds
import com.lockmedia.applock.main.ui.MainActivity
import com.lockmedia.applock.other.setAppDesign
import com.lockmedia.applock.securityapp.EncryptionManager
import com.lockmedia.applock.settings.data.Config
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Base Application class.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltAndroidApp
class BaseApplication : Application(), LifecycleObserver {

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var encryptionManager: EncryptionManager

    val rawApplicationState = MutableLiveData(ApplicationState.LOCKED)

    private var ignoreNextTimeout = false

    var applicationState: ApplicationState
        get() = rawApplicationState.value!!
        set(value) = rawApplicationState.postValue(value)

    override fun onCreate() {
        super.onCreate()

        appOpenManager = AppOpenManager(this)
        MobileAds.initialize(
            this
        ) { }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setAppDesign(config.systemDesign)
        Log.d("aaaa","BaseApplication  class")
    }
    companion object {
        private var appOpenManager: AppOpenManager? = null
    }
    /**
     * Ignore next check for lock timeout.
     */
    fun ignoreNextTimeout() {
        ignoreNextTimeout = true
    }

    /**
     * Reset the [EncryptionManager], set [applicationState] to [ApplicationState.LOCKED] and start [MainActivity] with NEW_TESK.
     */
    fun lockApp() {
        encryptionManager.reset()
        applicationState = ApplicationState.LOCKED
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }



    }
