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

package com.lockmedia.applock.unlock.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import com.lockmedia.applock.R
import com.lockmedia.applock.databinding.FragmentUnlockBinding
import com.lockmedia.applock.other.extensions.*
import com.lockmedia.applock.uiunits.Dialogs
import com.lockmedia.applock.uiunits.base.BaseActivity
import com.lockmedia.applock.uiunits.bindings.BindableFragment

/**
 * Unlock fragment.
 * Handles state and login.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class UnlockFragment : BindableFragment<FragmentUnlockBinding>(R.layout.fragment_unlock) {

    private val viewModel: UnlockViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.addOnPropertyChange<UnlockState>(com.lockmedia.applock.BR.unlockState) {
            when (it) {
                UnlockState.CHECKING -> binding.loadingOverlay.show()
                UnlockState.UNLOCKED -> unlock()
                UnlockState.LOCKED -> {
                    binding.loadingOverlay.hide()
                    binding.unlockWrongPasswordWarningTextView.show()
                }
                else -> return@addOnPropertyChange
            }
        }

        viewModel.addOnPropertyChange<String>(com.lockmedia.applock.BR.password) {
            if (binding.unlockWrongPasswordWarningTextView.visibility != View.INVISIBLE) {
                binding.unlockWrongPasswordWarningTextView.vanish()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun unlock() {
        requireActivityAs(BaseActivity::class).hideKeyboard()
        binding.loadingOverlay.hide()

        if (viewModel.encryptionManager.isReady) {
            requireActivity().getBaseApplication().applicationState = com.lockmedia.applock.ApplicationState.UNLOCKED
            findNavController().navigate(R.id.action_unlockFragment_to_galleryFragment)
        } else {
            Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
        }
    }
    override fun bind(binding: FragmentUnlockBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel


        // Set different text colors for parts of the text

        val unlock_desc=getString(R.string.unlock_title_desc)
        val spannableStringDesc=SpannableString(unlock_desc)

        val photo = "photo"
        val video = "video"

        val start_photo = unlock_desc.indexOf(photo)
        val end_photo = start_photo + photo.length

        val start_video = unlock_desc.indexOf(video)
        val end_video = start_video + video.length
        if (start_photo >= 0 && end_photo <= unlock_desc.length){
            spannableStringDesc.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorAccent)), start_photo, end_photo, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        if(start_video>=0 && end_video<=unlock_desc.length)
        {
            spannableStringDesc.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorAccent)), start_video, end_video, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.unlockDes.text = spannableStringDesc


        val unlock_title = getString(R.string.unlock_title)
        val spannableString = SpannableString(unlock_title)
        // Define the range for the first part of the text and set its color

        // Define the range for the second part of the text and set its color
        val safe_text = "Safe"

        val safe_start=unlock_title.indexOf(safe_text)
        val saf_end =safe_start+safe_text.length

        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorAccent)), safe_start, saf_end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        // Set the modified spannable string to the TextView
        binding.unlockTitle.text = spannableString
    }

}