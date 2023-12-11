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

package com.lockmedia.applock.backupData.ui

import android.app.Application
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lockmedia.applock.other.extensions.empty
import com.lockmedia.applock.securityapp.PasswordManager
import com.lockmedia.applock.uiunits.bindings.ObservableViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for unlocking a Backup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class UnlockBackupViewModel @Inject constructor(
    app: Application,
    private val passwordManager: PasswordManager
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(com.lockmedia.applock.BR.password, value)
        }

    /**
     * Verifies the password and calls [result] with true/false.
     */
    fun verifyPassword(backupPassword: String, result: (valid: Boolean) -> Unit) =
        viewModelScope.launch {
            val valid = passwordManager.checkPassword(password, backupPassword)
            result(valid)
        }
}