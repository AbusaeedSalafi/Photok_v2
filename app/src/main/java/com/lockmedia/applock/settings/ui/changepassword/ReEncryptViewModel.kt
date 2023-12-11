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

package com.lockmedia.applock.settings.ui.changepassword

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lockmedia.applock.models.database.entity.Photo
import com.lockmedia.applock.models.io.EncryptedStorageManager
import com.lockmedia.applock.models.repositories.PhotoRepository
import com.lockmedia.applock.securityapp.EncryptionManager
import com.lockmedia.applock.securityapp.PasswordManager
import com.lockmedia.applock.uiunits.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * ViewModel for re-encrypting photos with a new password.
 * Executed after password change.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ReEncryptViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val passwordManager: PasswordManager
) : BaseProcessViewModel<Photo>(app) {

    lateinit var newPassword: String

    override suspend fun preProcess() {
        items = photoRepository.getAll()
        elementsToProcess = items.size
        super.preProcess()
    }

    override suspend fun processItem(item: Photo) {
        val fileSuccess = encryptedStorageManager.reEncryptFile(item.internalFileName, newPassword)
        val thumbnailSuccess =
            encryptedStorageManager.reEncryptFile(item.internalThumbnailFileName, newPassword)

        val videoPreviewSuccess = if (item.type.isVideo) {
            encryptedStorageManager.reEncryptFile(item.internalVideoPreviewFileName, newPassword)
        } else {
            true // Just set true, since it can be ignored
        }

        if (!fileSuccess || !thumbnailSuccess || !videoPreviewSuccess) {
            failuresOccurred()
            return
        }
    }

    override suspend fun postProcess() {
        super.postProcess()
        passwordManager.storePassword(newPassword)
        encryptionManager.initialize(newPassword)
    }
}