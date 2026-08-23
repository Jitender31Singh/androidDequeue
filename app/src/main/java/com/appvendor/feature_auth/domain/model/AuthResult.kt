package com.appvendor.feature_auth.domain.model

/**
 * Represents the result of an authentication operation.
 */
sealed class AuthResult {
    data class Success(val user: User, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
