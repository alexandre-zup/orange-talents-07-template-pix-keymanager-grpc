package dev.alexandrevieira.manager.apiclients.bcb.dto

data class BcbCreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO
)