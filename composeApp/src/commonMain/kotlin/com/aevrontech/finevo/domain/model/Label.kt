package com.aevrontech.finevo.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Label for tagging transactions
 *
 * Labels can be assigned to transactions for additional categorization. When autoAssign is true,
 * the label is automatically applied to new transactions.
 */
@Serializable
data class Label(
    val id: String,
    val userId: String,
    val name: String,
    val color: String, // Hex color code e.g. "#FF5733"
    val autoAssign: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant
)
