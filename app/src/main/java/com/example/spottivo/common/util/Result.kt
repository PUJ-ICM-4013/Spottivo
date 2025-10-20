package com.example.spottivo.common.util
sealed interface Result<out T> {
    data class Ok<T>(val data: T): Result<T>
    data class Err(val message: String, val cause: Throwable? = null): Result<Nothing>
}
