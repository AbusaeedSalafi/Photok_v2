/*
 *   Copyright 2020-2023 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.lockmedia.applock.splashscreen.ui

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lockmedia.applock.settings.data.Config
import com.lockmedia.applock.uiunits.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * ViewModel to check the application state.
 * Used by SplashScreen.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    app: Application,
    private val config: Config
) : ObservableViewModel(app) {

    @get:Bindable
    var appStartState: AppStartState? = null
        set(value) {
            field = value
            notifyChange(com.lockmedia.applock.BR.appStartState, value)
        }

    /**
     * Check the application state.
     */
    fun checkApplicationState() = viewModelScope.launch {

        // First start
        if (config.systemFirstStart) {
            appStartState = AppStartState.FIRST_START
            return@launch
        }

        // Unlock or Setup
        val password = config.securityPassword
        appStartState = if (password == null || password.isEmpty()) {
            AppStartState.SETUP
        } else {
            AppStartState.LOCKED
        }
    }
}