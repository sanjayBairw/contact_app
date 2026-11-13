package com.snj.contactly.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

object IntentUtils {

    sealed class IntentLaunchResult {
        data object Success : IntentLaunchResult()
        data class Failure(val reason: String) : IntentLaunchResult()
    }

    fun buildDialIntent(phoneNumber: String): Intent {
        val sanitized = phoneNumber.trim()
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${Uri.encode(sanitized)}")
        }
    }

    fun buildSmsIntent(phoneNumber: String): Intent {
        val sanitized = phoneNumber.trim()
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("sms:${Uri.encode(sanitized)}")
        }
    }

    fun buildEmailIntent(emailAddress: String, subject: String? = null): Intent {
        val sanitized = emailAddress.trim()
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$sanitized")
            if (!subject.isNullOrBlank()) {
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
        }
    }

    fun launchDialer(context: Context, phoneNumber: String): IntentLaunchResult {
        if (phoneNumber.isBlank()) {
            return IntentLaunchResult.Failure("Phone number is unavailable")
        }
        return launchSafely(
            context = context,
            intent = buildDialIntent(phoneNumber),
            missingAppMessage = "No phone app found on this device"
        )
    }

    fun launchSms(context: Context, phoneNumber: String): IntentLaunchResult {
        if (phoneNumber.isBlank()) {
            return IntentLaunchResult.Failure("Phone number is unavailable")
        }
        return launchSafely(
            context = context,
            intent = buildSmsIntent(phoneNumber),
            missingAppMessage = "No messaging app found on this device"
        )
    }

    fun launchEmail(context: Context, emailAddress: String?): IntentLaunchResult {
        val email = emailAddress?.trim().orEmpty()
        if (email.isEmpty()) {
            return IntentLaunchResult.Failure("No email address saved for this contact")
        }
        return launchSafely(
            context = context,
            intent = buildEmailIntent(email),
            missingAppMessage = "No email app found on this device"
        )
    }

    private fun launchSafely(
        context: Context,
        intent: Intent,
        missingAppMessage: String
    ): IntentLaunchResult {
        return try {
            context.startActivity(intent)
            IntentLaunchResult.Success
        } catch (_: ActivityNotFoundException) {
            IntentLaunchResult.Failure(missingAppMessage)
        } catch (_: SecurityException) {
            IntentLaunchResult.Failure("Unable to open external app")
        }
    }
}

