package com.aevrontech.finevo.core.util

/**
 * A generic sealed class representing the result of an operation.
 * 
 * @param T The type of data in case of success
 */
sealed class Result<out T> {
    /**
     * Represents a successful result with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed result with an error
     */
    data class Error(val exception: AppException) : Result<Nothing>()

    /**
     * Represents a loading state
     */
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this is a Success
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this is an Error
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns true if this is Loading
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if Success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the data if Success, throws the exception if Error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    /**
     * Returns the data if Success, or the default value
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * Maps the success value to another type
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Flat maps the success value to another Result
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Runs the block if this is a Success
     */
    inline fun onSuccess(block: (T) -> Unit): Result<T> {
        if (this is Success) block(data)
        return this
    }

    /**
     * Runs the block if this is an Error
     */
    inline fun onError(block: (AppException) -> Unit): Result<T> {
        if (this is Error) block(exception)
        return this
    }

    /**
     * Runs the block if this is Loading
     */
    inline fun onLoading(block: () -> Unit): Result<T> {
        if (this is Loading) block()
        return this
    }

    companion object {
        /**
         * Creates a Success result
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Creates an Error result
         */
        fun <T> error(exception: AppException): Result<T> = Error(exception)

        /**
         * Creates a Loading result
         */
        fun <T> loading(): Result<T> = Loading

        /**
         * Wraps a suspend function call in a Result
         */
        inline fun <T> runCatching(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: AppException) {
            Error(e)
        } catch (e: Exception) {
            Error(AppException.Unknown(e.message ?: "Unknown error", e))
        }

        /**
         * Wraps a suspend function call in a Result
         */
        suspend inline fun <T> runCatchingSuspend(crossinline block: suspend () -> T): Result<T> = try {
            Success(block())
        } catch (e: AppException) {
            Error(e)
        } catch (e: Exception) {
            Error(AppException.Unknown(e.message ?: "Unknown error", e))
        }
    }
}
