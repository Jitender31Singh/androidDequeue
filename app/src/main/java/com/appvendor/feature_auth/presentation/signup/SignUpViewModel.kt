package com.appvendor.feature_auth.presentation.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appvendor.core.network.ApiResult
import com.appvendor.core.utils.Validators
import com.appvendor.feature_auth.domain.model.AuthResult
import com.appvendor.feature_auth.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    fun onNameChange(name: String) = _state.update { it.copy(name = name, nameError = null, error = null) }
    fun onShopNameChange(shopName: String) = _state.update { it.copy(shopName = shopName, shopNameError = null, error = null) }
    fun onPhoneChange(phone: String) = _state.update { it.copy(phone = phone, phoneError = null, error = null) }
    fun onEmailChange(email: String) = _state.update { it.copy(email = email, emailError = null, error = null) }
    fun onPasswordChange(password: String) = _state.update { it.copy(password = password, passwordError = null, error = null) }

    fun onSignUp() {
        val nameError = Validators.validateName(_state.value.name)
        val shopError = Validators.validateShopName(_state.value.shopName)
        val phoneError = Validators.validatePhone(_state.value.phone)
        val emailError = Validators.validateEmail(_state.value.email)
        val passwordError = Validators.validatePassword(_state.value.password)

        if (nameError != null || shopError != null || phoneError != null || emailError != null || passwordError != null) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    shopNameError = shopError,
                    phoneError = phoneError,
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            signUpUseCase(
                name = _state.value.name,
                shopName = _state.value.shopName,
                phone = _state.value.phone,
                email = _state.value.email,
                password = _state.value.password
            ).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is ApiResult.Success -> {
                        when (result.data) {
                            is AuthResult.Success -> _state.update { it.copy(isLoading = false, isSuccess = true) }
                            is AuthResult.Error -> _state.update { it.copy(isLoading = false, error = result.data.message) }
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
}
