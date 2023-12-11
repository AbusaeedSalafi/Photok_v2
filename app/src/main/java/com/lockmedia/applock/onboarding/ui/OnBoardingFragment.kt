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

package com.lockmedia.applock.onboarding.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import dagger.hilt.android.AndroidEntryPoint
import com.lockmedia.applock.R
import com.lockmedia.applock.databinding.FragmentOnboardingBinding
import com.lockmedia.applock.settings.data.Config
import com.lockmedia.applock.uiunits.ViewPagerAdapter
import com.lockmedia.applock.uiunits.bindings.BindableFragment
import javax.inject.Inject

/**
 * On boarding fragment.
 * Used as a "tutorial".
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class OnBoardingFragment :
    BindableFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {

    @Inject
    lateinit var config: Config

    private var isLastPage = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.onBoardingDotSelector1.isSelected = true
        binding.onBoardingDotSelector2.isSelected = false
        binding.onBoardingDotSelector3.isSelected = false

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(Fragment(R.layout.fragment_onboarding_slide_1))
        viewPagerAdapter.addFragment(Fragment(R.layout.fragment_onboarding_slide_2))
        viewPagerAdapter.addFragment(Fragment(R.layout.fragment_onboarding_slide_3))
        binding.onBoardingViewPager.adapter = viewPagerAdapter
        binding.onBoardingViewPager.addOnPageChangeListener(onPageChangeListener)
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            binding.onBoardingButton.text = when (position) {
                0 -> {
                    binding.onBoardingDotSelector1.isSelected = true
                    binding.onBoardingDotSelector1.setImageResource(R.drawable.slider_selector_)
                    binding.onBoardingDotSelector2.setImageResource(R.drawable.slider_selected_dot) // Set the unselected image for other dots
                    binding.onBoardingDotSelector3.setImageResource(R.drawable.slider_selected_dot)
                    binding.onBoardingDotSelector2.isSelected = false
                    binding.onBoardingDotSelector3.isSelected = false
                    getString(R.string.onboarding_next)
                }
                1 -> {
                    binding.onBoardingDotSelector1.isSelected = false
                    binding.onBoardingDotSelector2.isSelected = true
                    binding.onBoardingDotSelector1.setImageResource(R.drawable.slider_selected_dot)
                    binding.onBoardingDotSelector2.setImageResource(R.drawable.slider_selector_)
                    binding.onBoardingDotSelector3.setImageResource(R.drawable.slider_selected_dot)
                    binding.onBoardingDotSelector3.isSelected = false
                    getString(R.string.onboarding_next)
                }
                2 -> {
                    binding.onBoardingDotSelector1.isSelected = false
                    binding.onBoardingDotSelector2.isSelected = false
                    binding.onBoardingDotSelector3.isSelected = true
                    binding.onBoardingDotSelector1.setImageResource(R.drawable.slider_selected_dot)
                    binding.onBoardingDotSelector2.setImageResource(R.drawable.slider_dot_selector)
                    binding.onBoardingDotSelector3.setImageResource(R.drawable.slider_selector_)
                    getString(R.string.onboarding_finish)
                }
                else -> getString(R.string.onboarding_next)
            }
            isLastPage = position == 2
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    /**
     * Swipe to next slide or finish
     * Called by ui.
     */
    fun buttonClicked() {
        if (isLastPage) {
            finish()
        } else {
            binding.onBoardingViewPager.setCurrentItem(
                binding.onBoardingViewPager.currentItem + 1,
                true
            )
        }
    }

    /**
     * Navigate to setup and set first start to false.
     */
    fun finish() {
        findNavController().navigate(R.id.action_onBoardingFragment_to_setupFragment)
        config.systemFirstStart = false
    }

    override fun bind(binding: FragmentOnboardingBinding) {
        super.bind(binding)
        binding.context = this
    }
}