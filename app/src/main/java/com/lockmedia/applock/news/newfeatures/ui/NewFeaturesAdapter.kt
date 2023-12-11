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

package com.lockmedia.applock.news.newfeatures.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lockmedia.applock.databinding.ItemNewsBinding
import com.lockmedia.applock.news.newfeatures.ui.model.NewFeatureViewData

/**
 * Adapter for news entries in the [NewFeaturesDialog].
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewFeaturesAdapter(
    private val featureViewData: List<NewFeatureViewData>,
) : RecyclerView.Adapter<NewFeaturesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewFeaturesViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context))
        return NewFeaturesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewFeaturesViewHolder, position: Int) {
        holder.bindTo(featureViewData[position])
    }

    override fun getItemCount(): Int = featureViewData.size
}