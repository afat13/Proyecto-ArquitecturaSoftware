package com.example.aprendeaprender.viewmodel

import androidx.annotation.StringRes

sealed interface AuthEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToLogin : AuthEvent
    data object NavigateToVerifyEmail : AuthEvent
    data object NavigateToResetPasswordEmailSent : AuthEvent
    data class ShowSnackbar(@StringRes val messageResId: Int) : AuthEvent
}