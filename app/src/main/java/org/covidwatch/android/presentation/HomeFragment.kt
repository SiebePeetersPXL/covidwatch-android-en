package org.covidwatch.android.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.covidwatch.android.BuildConfig
import org.covidwatch.android.R
import org.covidwatch.android.databinding.FragmentHomeBinding
import org.covidwatch.android.domain.FirstTimeUser
import org.covidwatch.android.domain.ReturnUser
import org.covidwatch.android.domain.Setup
import org.covidwatch.android.presentation.home.HomeViewModel
import org.covidwatch.android.presentation.home.InfoBanner
import org.covidwatch.android.presentation.home.InfoBannerViewModel
import org.covidwatch.android.presentation.home.WarningBanner
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val infoBannerViewModel: InfoBannerViewModel by viewModel()
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        infoBannerViewModel.onStart()
        infoBannerViewModel.infoBanner.observe(viewLifecycleOwner, Observer { banner ->
            when (banner) {
                is InfoBanner.Show -> {
                    binding.infoBanner.isVisible = true
                    binding.infoBanner.setText(banner.text)
                }
                InfoBanner.Hide -> {
                    binding.infoBanner.isVisible = false
                }
            }
        })

        homeViewModel.onStart()
        homeViewModel.userFlow.observe(viewLifecycleOwner, Observer { userFlow ->
            when (userFlow) {
                is FirstTimeUser -> {
                    updateUiForFirstTimeUser()
                }
                is Setup -> {
                    findNavController().navigate(R.id.splashFragment)
                }
                is ReturnUser -> {
                    updateUiForReturnUser()
                }
            }
        })
        homeViewModel.warningBanner.observe(viewLifecycleOwner, Observer { banner ->
            when (banner) {
                is WarningBanner.Show -> {
                    binding.warningBanner.isVisible = true
                    binding.warningBanner.setText(banner.text)
                }
                WarningBanner.Hide -> {
                    binding.warningBanner.isVisible = false
                }
            }
        })
        homeViewModel.userTestedPositive.observe(viewLifecycleOwner, Observer {
            updateUiForTestedPositive()
        })

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.testedButton.setOnClickListener {
            findNavController().navigate(R.id.testQuestionsFragment)
        }
        binding.toolbar.menuButton.setOnClickListener {
            findNavController().navigate(R.id.menuFragment)
        }
        binding.shareAppButton.setOnClickListener {
            shareApp()
        }
        binding.warningBanner.setOnClickListener {
            findNavController().navigate(R.id.potentialRiskFragment)
        }
        binding.infoBanner.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
    }

    private fun shareApp() {
        val shareText = getString(R.string.share_intent_text)
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "$shareText https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_text)))
    }

    private fun updateUiForFirstTimeUser() {
        binding.homeTitle.setText(R.string.you_re_all_set_title)
        binding.homeSubtitle.setText(R.string.thank_you_text)
    }

    private fun updateUiForReturnUser() {
        binding.homeTitle.setText(R.string.welcome_back_title)
        binding.homeSubtitle.setText(R.string.not_detected_text)
    }

    private fun updateUiForTestedPositive() {
        binding.homeSubtitle.setText(R.string.reported_tested_positive_text)
        binding.testedButton.isVisible = false
        binding.testedButtonText.isVisible = false
    }
}