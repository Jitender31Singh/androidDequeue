package com.appvendor.feature_auth.domain.usecase

import com.appvendor.core.network.ApiResult
import com.appvendor.core.utils.Validators
import com.appvendor.feature_auth.domain.model.AuthResult
import com.appvendor.feature_auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<ApiResult<AuthResult>> = flow {
        emit(ApiResult.Loading)
        
        val emailError = Validators.validateEmail(email)
        val passwordError = Validators.validatePassword(password)
        
        if (emailError != null || passwordError != null) {
            emit(ApiResult.Error(message = "Invalid input"))
            return@flow
        }
        
        val result = repository.login(email, password)
        emit(result)
    }
}
