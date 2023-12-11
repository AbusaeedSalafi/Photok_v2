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

package com.lockmedia.applock.gallerymedia.ui.importing

import android.app.Application
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import com.lockmedia.applock.models.repositories.PhotoRepository
import com.lockmedia.applock.uiunits.base.processdialogs.BaseProcessViewModel
import javax.inject.Inject

/**
 * View model to handle importing photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class ImportViewModel @Inject constructor(
    app: Application,
    private val photoRepository: PhotoRepository
) : BaseProcessViewModel<Uri>(app) {

    override suspend fun processItem(item: Uri) {
        val success = photoRepository.safeImportPhoto(item)
        if (!success) {
            failuresOccurred = true
        }
    }
}