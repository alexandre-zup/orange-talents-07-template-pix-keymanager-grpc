package dev.alexandrevieira.manager.apiclients.bcb.dto

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class BcbDeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)
