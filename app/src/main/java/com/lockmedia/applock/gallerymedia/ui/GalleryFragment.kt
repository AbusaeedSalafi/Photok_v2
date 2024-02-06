package com.lockmedia.applock.gallerymedia.ui

import android.Manifest
import android.content.ContentValues.TAG
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lockmedia.applock.R
import com.lockmedia.applock.databinding.FragmentGalleryBinding
import com.lockmedia.applock.gallerymedia.ui.importing.ImportMenuDialog
import com.lockmedia.applock.gallerymedia.ui.menu.DeleteBottomSheetDialogFragment
import com.lockmedia.applock.gallerymedia.ui.menu.ExportBottomSheetDialogFragment
import com.lockmedia.applock.main.ui.MainActivity
import com.lockmedia.applock.news.newfeatures.ui.NewFeaturesDialog
import com.lockmedia.applock.other.INTENT_PHOTO_ID
import com.lockmedia.applock.other.REQ_PERM_EXPORT
import com.lockmedia.applock.other.extensions.hide
import com.lockmedia.applock.other.extensions.requireActivityAs
import com.lockmedia.applock.other.extensions.show
import com.lockmedia.applock.uiunits.Dialogs
import com.lockmedia.applock.uiunits.bindings.BindableFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


/**
 * Fragment for displaying a gallery.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class GalleryFragment : BindableFragment<FragmentGalleryBinding>(R.layout.fragment_gallery) {
    private val handler = Handler()
    private val viewModel: GalleryViewModel by viewModels()

    private lateinit var adapter: PhotoAdapter


    //admob implementation instance
    private var mInterstitialAd: InterstitialAd? = null
    var adRequest = AdRequest.Builder().build()
    private var isAdShown = false
    lateinit var mAdView : AdView
    private var actionMode: ActionMode? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        setToolbar(binding.galleryToolbar)
        setupGridView()

        MobileAds.initialize(requireContext()) {}
        mAdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

         // bannerAdsfn()
        loadInterstitialAd()
        adapter.isMultiSelectMode.observe(viewLifecycleOwner) {
            if (it) {
                actionMode = (activity as MainActivity).startSupportActionMode(actionModeCallback)
            } else {
                actionMode?.finish()
            }
        }

        viewModel.runIfNews {
            NewFeaturesDialog().show(requireActivity().supportFragmentManager)
        }
        val customMenuLayout = view.findViewById<ConstraintLayout>(R.id.customMenu) // Find the included layout by its ID

// Make sure customMenuLayout is empty before inflating the layout
        customMenuLayout.removeAllViews()
        //val lock_menu = customMenuLayout.findViewById<View>(R.id.lock_menu)
        val menuItem1 = layoutInflater.inflate(R.layout.custom_menu_lock, customMenuLayout)

        menuItem1.setOnClickListener {
            // Handle the click event for MenuItem 1
            findNavController().navigate(R.id.action_galleryFragment_to_unlockFragment)
            // requireActivity().getBaseApplication().lockApp()
        }

        val customNavigationLayout =
            view.findViewById<LinearLayout>(R.id.customNavigationLayout) // Find the included layout by its ID


// Find the individual views within the custom layout

        val settingItem = customNavigationLayout.findViewById<View>(R.id.settingItem)
        val settingText = settingItem.findViewById<TextView>(R.id.settingText)
        val settingIcon = settingItem.findViewById<ImageView>(R.id.settingIcon)

        val homeItem = customNavigationLayout.findViewById<View>(R.id.homeItem)
        val homeIcon = homeItem.findViewById<ImageView>(R.id.homeIcon)
        val homeText = homeItem.findViewById<TextView>(R.id.homeText)

        homeItem.setBackgroundResource(R.drawable.rounded_background)
        settingItem.setBackgroundColor(Color.TRANSPARENT)
        settingText.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlue))
        settingIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorBlue))

        settingItem.setOnClickListener {

            // Handle the click event for the "Settings" item

            if (findNavController().currentDestination?.id != R.id.settingsFragment) {

                // Navigate to the Settings fragment
                findNavController().navigate(R.id.action_galleryFragment_to_settingsHomeFragment)
                settingItem.setBackgroundResource(R.drawable.rounded_background)
                settingText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                settingIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))

                // Reset the "Home" item's background and text/icon colors
                homeItem.setBackgroundColor(Color.TRANSPARENT)
                homeText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                homeIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorBlue))
            }
        }


    }



    fun showInterstitialAd() {
        val delayMillis: Long = 1500 // 1.5 seconds

        val runnable: Runnable = object : Runnable {
            override fun run() {
                // Use a Handler to delay the ad loading
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(requireActivity())
                    isAdShown = true
                } else {
                    showImportMenu()
                }
                handler.removeCallbacks(this)
            }
        }

        // Post the runnable with the specified delay
        handler.postDelayed(runnable, delayMillis)
    }

    private fun loadInterstitialAd() {
        InterstitialAd.load(
            requireContext(),
            getString(R.string.inter_ad_unit_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError?.toString()?.let { Log.d(TAG, it) }
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    mInterstitialAd = interstitialAd
                    mInterstitialAd!!.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                super.onAdClicked()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                // Reset the flag when ad is dismissed
                                isAdShown = false
                                // Reload the ad for the next time
                                showImportMenu()
                                loadInterstitialAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                super.onAdFailedToShowFullScreenContent(adError)
                                mInterstitialAd = null
                                // Reload the ad for the next time
                                loadInterstitialAd()
                            }
                        }
                }
            })
    }

    private fun setupGridView() {
        binding.galleryPhotoGrid.layoutManager = GridLayoutManager(requireContext(), getColCount())

        adapter = PhotoAdapter(
            requireContext(),
            viewModel.photoRepository,
            this::openPhoto,
            viewLifecycleOwner
        )

        adapter.registerAdapterDataObserver(onAdapterDataObserver)
        binding.galleryPhotoGrid.adapter = adapter
        lifecycleScope.launch {
            viewModel.photos.collectLatest { adapter.submitData(it) }
        }
    }

    /**
     * Show [ImportMenuDialog].
     * Called by ui.
     */
    fun showImportMenu() {
        ImportMenuDialog().show(childFragmentManager)
    }

    /**
     * Start the deleting process with all selected items.
     * Called by ui.
     */
    fun startDelete() {
        DeleteBottomSheetDialogFragment(adapter.getAllSelected()).show(requireActivity().supportFragmentManager)
        adapter.disableSelection()
    }

    /**
     * Starts the exporting process.
     * May request permission WRITE_EXTERNAL_STORAGE.
     * Called by ui.
     */
    @AfterPermissionGranted(REQ_PERM_EXPORT)
    fun startExport() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ) {
            ExportBottomSheetDialogFragment(adapter.getAllSelected()).show(requireActivity().supportFragmentManager)
            adapter.disableSelection()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.export_permission_rationale),
                REQ_PERM_EXPORT,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private val onAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            viewModel.togglePlaceholder(adapter.itemCount)
            updateFileAmount()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            viewModel.togglePlaceholder(adapter.itemCount)
            updateFileAmount()
        }
    }

    private fun updateFileAmount() {
        if (adapter.itemCount == 0) {
            binding.galleryAllPhotosAmount.hide()
        } else {
            binding.galleryAllPhotosAmount.show()
            binding.galleryAllPhotosAmount.text = "(${adapter.itemCount})"
        }
    }

    private fun getColCount() = when (resources.configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> 4
        Configuration.ORIENTATION_LANDSCAPE -> 8
        else -> 4
    }



    private fun openPhoto(id: Int) {
        val args = bundleOf(INTENT_PHOTO_ID to id)
        findNavController().navigate(R.id.action_galleryFragment_to_imageViewerFragment, args)
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_multi_select, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean =
            when (item?.itemId) {
                R.id.menuMsAll -> {
                    lifecycleScope.launch {
                        adapter.selectAll()
                    }
                    true
                }

                R.id.menuMsDelete -> {
                    lifecycleScope.launch {
                        Dialogs.showConfirmDialog(
                            requireContext(),
                            String.format(
                                getString(R.string.delete_are_you_sure),
                                adapter.selectedItems.size
                            )
                        ) { _, _ -> // On positive button clicked
                            startDelete()
                        }
                    }
                    true
                }

                R.id.menuMsExport -> {
                    lifecycleScope.launch {
                        Dialogs.showConfirmDialog(
                            requireContext(),
                            String.format(
                                getString(R.string.export_are_you_sure),
                                adapter.selectedItems.size
                            )
                        ) { _, _ -> // On positive button clicked
                            startExport()
                        }
                    }
                    true
                }

                else -> false
            }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.disableSelection()
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivityAs(MainActivity::class).onOrientationChanged = {
            setupGridView()
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivityAs(MainActivity::class).onOrientationChanged = {} // Reset
    }

    override fun bind(binding: FragmentGalleryBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }

}