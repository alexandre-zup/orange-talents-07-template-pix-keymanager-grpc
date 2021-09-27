package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.endpoints.consulta.ChavePixInfoResponse
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime

@Introspected
data class BcbPixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountDTO,
    val owner: OwnerDTO,
    val createdAt: LocalDateTime
)