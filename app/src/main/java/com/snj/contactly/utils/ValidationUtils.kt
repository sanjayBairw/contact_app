package com.snj.contactly.utils

object ValidationUtils {

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    private val PHONE_ALLOWED_CHARS_REGEX = Regex("^[+]?[0-9\\s().-]+$")

    data class ContactFormValidation(
        val nameError: String? = null,
        val phoneError: String? = null,
        val emailError: String? = null
    ) {
        val isValid: Boolean
            get() = nameError == null && phoneError == null && emailError == null
    }

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Full name is required"
            trimmed.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        val trimmed = phone.trim()
        if (trimmed.isEmpty()) {
            return "Phone number is required"
        }
        if (!PHONE_ALLOWED_CHARS_REGEX.matches(trimmed)) {
            return "Enter a valid phone number"
        }
        val digitsCount = trimmed.count { it.isDigit() }
        if (digitsCount !in 7..15) {
            return "Phone number must have 7 to 15 digits"
        }
        return null
    }

    fun validateEmail(email: String?): String? {
        val trimmed = email?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return null
        }
        return if (EMAIL_REGEX.matches(trimmed)) {
            null
        } else {
            "Enter a valid email address"
        }
    }

    fun validateContactForm(
        name: String,
        phone: String,
        email: String?
    ): ContactFormValidation {
        return ContactFormValidation(
            nameError = validateName(name),
            phoneError = validatePhone(phone),
            emailError = validateEmail(email)
        )
    }
}

