package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.data.model.enums.TipoChave
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertThrows

internal class KeyTypeTest {
    @Nested
    inner class CPF {
        @Test
        @DisplayName("Deve converter tipo CPF")
        internal fun deveConverterTipoCpf() {
            val tipo: TipoChave = KeyType.CPF.convert()

            assertEquals(TipoChave.CPF, tipo)
        }
    }

    @Nested
    inner class CNPJ {
        @Test
        @DisplayName("Deve falhar ao converter tipo CNPJ")
        internal fun deveFalharAoConverterTipoCnpj() {
            assertThrows<NotImplementedError> { KeyType.CNPJ.convert() }
        }
    }

    @Nested
    inner class PHONE {
        @Test
        @DisplayName("Deve converter tipo PHONE")
        internal fun deveConverterTipoPhone() {
            val tipo: TipoChave = KeyType.PHONE.convert()

            assertEquals(TipoChave.CELULAR, tipo)
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        @DisplayName("Deve converter tipo EMAIL")
        internal fun deveConverterTipoEmail() {
            val tipo: TipoChave = KeyType.EMAIL.convert()

            assertEquals(TipoChave.EMAIL, tipo)
        }
    }

    @Nested
    inner class RANDOM {
        @Test
        @DisplayName("Deve converter tipo RANDOM")
        internal fun deveConverterTipoRandom() {
            val tipo: TipoChave = KeyType.RANDOM.convert()

            assertEquals(TipoChave.ALEATORIA, tipo)
        }
    }
}