package dev.alexandrevieira.manager.apiclients.bcb.dto

import java.time.LocalDateTime

data class BcbCreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO,
    val createdAt: LocalDateTime
)