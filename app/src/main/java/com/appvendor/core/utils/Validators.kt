package com.appvendor.core.utils

import android.util.Patterns

/**
 * Utility object containing validation functions for user input.
 */
object Validators {

    /**
     * Validates an email address.
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates a password.
     * Requirements: at least 8 characters, does not contain SQL injection characters.
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val containsSqlInjection = password.contains("'") || password.contains("\"") || 
                password.contains(";") || password.contains("--") || 
                password.contains("/*") || password.contains("*/")
        return !containsSqlInjection
    }

    /**
     * Validates a phone number.
     * Simple validation checking if it's not blank and matches phone pattern.
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        return phone.isNotBlank() && Patterns.PHONE.matcher(phone).matches()
    }

    /**
     * Validates a generic name field.
     */
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email address"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.length < 8) return "Password must be at least 8 characters"
        val containsSqlInjection = password.contains("'") || password.contains("\"") || 
                password.contains(";") || password.contains("--") || 
                password.contains("/*") || password.contains("*/")
        if (containsSqlInjection) {
            return "Password contains invalid characters"
        }
        return null
    }

    fun validatePhone(phone: String): String? {
        if (phone.isBlank()) return "Phone number cannot be empty"
        if (!Patterns.PHONE.matcher(phone).matches()) return "Invalid phone number"
        return null
    }

    fun validateName(name: String): String? {
        if (name.trim().length < 2) return "Name must be at least 2 characters"
        return null
    }

    fun validateShopName(shopName: String): String? {
        if (shopName.trim().length < 2) return "Shop name must be at least 2 characters"
        return null
    }
}
