package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.data.model.Conta
import io.micronaut.core.annotation.Introspected

@Introspected
data class BankAccountDTO(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {
    constructor(conta: Conta) : this(conta.instituicao.ispb, conta.agencia, conta.numero, conta.tipo.converte())
}