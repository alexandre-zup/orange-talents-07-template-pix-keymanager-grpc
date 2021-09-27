package dev.alexandrevieira.manager.apiclients.bcb.dto

data class BcbDeletePixKeyRequest(
    val key: String,
    val participant: String
)
