package dev.alexandrevieira.manager.apiclients.bcb.dto

import io.micronaut.core.annotation.Introspected

@Introspected
data class BcbCreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO
)