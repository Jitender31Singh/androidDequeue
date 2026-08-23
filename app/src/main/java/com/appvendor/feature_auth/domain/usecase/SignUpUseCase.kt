package com.appvendor.feature_auth.domain.usecase

import com.appvendor.core.network.ApiResult
import com.appvendor.core.utils.Validators
import com.appvendor.feature_auth.domain.model.AuthResult
import com.appvendor.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(
        name: String,
        shopName: String,
        phone: String,
        email: String,
        password: String
    ): Flow<ApiResult<AuthResult>> = flow {
        emit(ApiResult.Loading)
        
        val nameError = Validators.validateName(name)
        val shopError = Validators.validateShopName(shopName)
        val phoneError = Validators.validatePhone(phone)
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)
        
        if (nameError != null || shopError != null || phoneError != null || emailError != null || passwordError != null) {
            emit(ApiResult.Error(message = "Invalid input fields"))
            return@flow
        }
        
        val result = repository.register(name, shopName, phone, email, password)
        emit(result)
    }
}
