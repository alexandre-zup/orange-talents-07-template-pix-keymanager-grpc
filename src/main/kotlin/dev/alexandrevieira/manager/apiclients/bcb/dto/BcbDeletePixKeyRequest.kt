package dev.alexandrevieira.manager.apiclients.bcb.dto

import io.micronaut.core.annotation.Introspected

@Introspected
data class BcbDeletePixKeyRequest(
    val key: String,
    val participant: String
)
