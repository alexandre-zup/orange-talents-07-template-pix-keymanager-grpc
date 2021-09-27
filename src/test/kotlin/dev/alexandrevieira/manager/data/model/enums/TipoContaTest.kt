package dev.alexandrevieira.manager.data.model.enums

import dev.alexandrevieira.manager.apiclients.bcb.dto.AccountType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class TipoContaTest {

    @Test
    @DisplayName("Deve converter conta corrente")
    internal fun deveConverterContaCorrente() {
        val accountType: AccountType = TipoConta.CONTA_CORRENTE.converte()
        assertEquals(AccountType.CACC, accountType)
    }

    @Test
    @DisplayName("Deve converter conta poupanca")
    internal fun deveConverterContaPoupanca() {
        val accountType: AccountType = TipoConta.CONTA_POUPANCA.converte()
        assertEquals(AccountType.SVGS, accountType)
    }
}