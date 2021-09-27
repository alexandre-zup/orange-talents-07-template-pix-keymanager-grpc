package dev.alexandrevieira.manager.apiclients.bcb.dto

import java.time.LocalDateTime

data class BcbDeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)
