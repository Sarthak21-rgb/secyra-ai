package com.secyra.ai.feature.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.core.telecom.DefaultPhoneRoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultDialerOnboardingViewModel @Inject constructor(
    private val defaultPhoneRoleManager: DefaultPhoneRoleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_SET_DEFAULT = "user_set_default_phone"
    }

    private val _uiState = MutableStateFlow(DefaultDialerOnboardingUiState())
    val uiState: StateFlow<DefaultDialerOnboardingUiState> = _uiState.asStateFlow()

    init {
        checkDefaultPhoneStatus()
    }

    fun checkDefaultPhoneStatus() {
        defaultPhoneRoleManager.checkDefaultPhoneStatus()
        _uiState.value = _uiState.value.copy(
            isDefaultPhoneApp = defaultPhoneRoleManager.isDefaultPhoneApp.value,
            canRequestRole = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                defaultPhoneRoleManager.isRoleRequestAvailable()
            } else {
                false
            }
        )
    }

    fun requestDefaultPhoneRole() {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = defaultPhoneRoleManager.createRequestRoleIntent()
                if (intent != null) {
                    _uiState.value = _uiState.value.copy(
                        requestIntent = intent,
                        shouldRequestRole = true,
                        isLoading = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Unable to request default phone role. Please set it manually in Android settings."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Default phone role requires Android 10 or higher. Please upgrade your device."
                )
            }
        }
    }

    fun handleRoleRequestResult(resultCode: Int) {
        viewModelScope.launch {
            if (resultCode == android.app.Activity.RESULT_OK) {
                // User approved the role
                checkDefaultPhoneStatus()
                _uiState.value = _uiState.value.copy(
                    shouldRequestRole = false,
                    isLoading = false,
                    errorMessage = null
                )
                
                // Mark that user has set as default
                sharedPrefs.edit()
                    .putBoolean(KEY_USER_SET_DEFAULT, true)
                    .apply()
                
                // Now request ANSWER_PHONE_CALLS permission
                _uiState.value = _uiState.value.copy(
                    shouldRequestAnswerCallPermission = true
                )
            } else {
                // User denied or cancelled
                _uiState.value = _uiState.value.copy(
                    shouldRequestRole = false,
                    isLoading = false,
                    errorMessage = "Default phone role was not granted. You can still use Secyra, but some features will be limited."
                )
            }
        }
    }

    fun handleAnswerCallPermissionResult(isGranted: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                shouldRequestAnswerCallPermission = false
            )
            
            if (isGranted) {
                // Permission granted - complete onboarding
                _uiState.value = _uiState.value.copy(
                    shouldComplete = true
                )
            } else {
                // Permission denied - allow skip
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Answer call permission was not granted. You can still use Secyra, but incoming call controls will be limited."
                )
            }
        }
    }

    fun complete() {
        _uiState.value = _uiState.value.copy(
            shouldComplete = true
        )
    }

    fun skip() {
        _uiState.value = _uiState.value.copy(
            shouldSkip = true
        )
    }
}

data class DefaultDialerOnboardingUiState(
    val isDefaultPhoneApp: Boolean = false,
    val canRequestRole: Boolean = false,
    val isLoading: Boolean = false,
    val requestIntent: Intent? = null,
    val shouldRequestRole: Boolean = false,
    val shouldRequestAnswerCallPermission: Boolean = false,
    val shouldComplete: Boolean = false,
    val shouldSkip: Boolean = false,
    val errorMessage: String? = null
)
