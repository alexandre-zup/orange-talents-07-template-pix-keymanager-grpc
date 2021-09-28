package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.data.model.enums.TipoConta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class AccountTypeTest {
    @Nested
    inner class CACC {
        @Test
        @DisplayName("Deve converter para conta corrente")
        internal fun deveConverterParaContaCorrente() {
            val tipo: TipoConta = AccountType.CACC.convert()

            assertEquals(TipoConta.CONTA_CORRENTE, tipo)
        }
    }

    @Nested
    inner class SVGS {
        @Test
        @DisplayName("Deve converter para conta poupanca")
        internal fun deveConverterParaContaPoupanca() {
            val tipo: TipoConta = AccountType.SVGS.convert()

            assertEquals(TipoConta.CONTA_POUPANCA, tipo)
        }
    }
}