package com.appvendor.feature_auth.presentation.signup

data class SignUpState(
    val name: String = "",
    val shopName: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val nameError: String? = null,
    val shopNameError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
