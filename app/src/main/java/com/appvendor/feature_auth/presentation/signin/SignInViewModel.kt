package com.appvendor.feature_auth.presentation.signin

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.BuildConfig
import com.appvendor.core.network.ApiResult
import com.appvendor.core.utils.Validators
import com.appvendor.feature_auth.domain.model.AuthResult
import com.appvendor.feature_auth.domain.usecase.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

import com.appvendor.core.datastore.UserPreferences
import com.appvendor.feature_auth.data.remote.AuthApiService
import com.appvendor.feature_auth.data.remote.dto.DeviceRegistrationRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
@SuppressLint("HardwareIds")
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val userPreferences: UserPreferences,
    private val authApiService: AuthApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()
    
    val rememberMe = userPreferences.rememberMe

    init {
        viewModelScope.launch {
            val isRememberMe = userPreferences.rememberMe.firstOrNull() ?: false
            if (isRememberMe) {
                val savedEmail = userPreferences.savedEmail.firstOrNull()
                if (!savedEmail.isNullOrBlank()) {
                    _state.update { it.copy(email = savedEmail) }
                }
            }
        }
    }
    
    fun onRememberMeChanged(remember: Boolean) {
        viewModelScope.launch {
            userPreferences.setRememberMe(remember, if (remember) _state.value.email else null)
        }
    }

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun onSignIn() {
        val emailError = Validators.validateEmail(_state.value.email)
        val passwordError = Validators.validatePassword(_state.value.password)

        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            signInUseCase(_state.value.email, _state.value.password).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is ApiResult.Success -> {
                        when (result.data) {
                            is AuthResult.Success -> {
                                val isRemember = userPreferences.rememberMe.firstOrNull() ?: false
                                if (isRemember) {
                                    userPreferences.setRememberMe(true, _state.value.email)
                                }
                                registerDeviceForFCM()
                                _state.update { it.copy(isLoading = false, isSuccess = true) }
                            }
                            is AuthResult.Error -> _state.update { it.copy(isLoading = false, error = result.data.message) }
                        }
                    }
                    is ApiResult.Error -> {
                        val errorMsg = if (result.code == 401 || result.code == 403 || result.code == 400 || result.message.contains("401", true)) {
                            "wrong mail and password"
                        } else {
                            result.message
                        }
                        _state.update { it.copy(isLoading = false, error = errorMsg) }
                    }
                }
            }
        }
    }

    private suspend fun registerDeviceForFCM() {
        try {
            // Add a timeout so the UI doesn't hang forever if Firebase token fetch stalls
            val fcmToken = kotlinx.coroutines.withTimeout(5000) {
                FirebaseMessaging.getInstance().token.await()
            }
            val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            
            val request = DeviceRegistrationRequest(
                deviceId = deviceId,
                fcmToken = fcmToken,
                deviceName = Build.MODEL,
                platform = "ANDROID",
                appVersion = BuildConfig.VERSION_NAME
            )
            
            authApiService.registerDevice(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
