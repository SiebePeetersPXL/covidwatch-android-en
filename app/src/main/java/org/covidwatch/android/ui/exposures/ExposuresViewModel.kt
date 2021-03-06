package org.covidwatch.android.ui.exposures

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.covidwatch.android.data.CovidExposureInformation
import org.covidwatch.android.data.exposureinformation.ExposureInformationRepository
import org.covidwatch.android.data.pref.PreferenceStorage
import org.covidwatch.android.exposurenotification.ExposureNotificationManager
import org.covidwatch.android.exposurenotification.ExposureNotificationManager.Companion.PERMISSION_START_REQUEST_CODE
import org.covidwatch.android.ui.BaseViewModel
import org.covidwatch.android.ui.event.Event

class ExposuresViewModel(
    private val enManager: ExposureNotificationManager,
    private val preferenceStorage: PreferenceStorage,
    exposureInformationRepository: ExposureInformationRepository
) : BaseViewModel() {

    private val _exposureNotificationEnabled = MutableLiveData<Boolean>()
    val exposureNotificationEnabled: LiveData<Boolean> = _exposureNotificationEnabled

    private val _enEnabled = MutableLiveData<Boolean>()
    val enEnabled: LiveData<Boolean> = _enEnabled

    private val _showExposureDetails = MutableLiveData<Event<CovidExposureInformation>>()
    val showExposureDetails: LiveData<Event<CovidExposureInformation>> = _showExposureDetails

    val exposureInfo: LiveData<List<CovidExposureInformation>> =
        exposureInformationRepository.exposureInformation().map { exposures ->
            exposures.sortedByDescending { it.date.toEpochMilli() }
        }

    val lastExposureTime
        get() = preferenceStorage.observableLastCheckedForExposures

    fun start() {
        viewModelScope.launch {
            _exposureNotificationEnabled.value = isExposureNotificationEnabled()
            _enEnabled.value = isExposureNotificationEnabled()
        }
    }

    fun enableExposureNotification(enable: Boolean) {
        viewModelScope.launch {
            val isEnabled = isExposureNotificationEnabled()

            when {
                enable && !isEnabled -> withPermission(PERMISSION_START_REQUEST_CODE) {
                    enManager.start().apply {
                        success {
                            _enEnabled.value = true
                        }
                    }
                }
                !enable && isEnabled -> {
                    _enEnabled.value = false
                    enManager.stop()
                }
            }
        }
    }

    private suspend fun isExposureNotificationEnabled() = !enManager.isDisabled()
}
