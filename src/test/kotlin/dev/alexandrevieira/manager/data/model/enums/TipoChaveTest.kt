package dev.alexandrevieira.manager.data.model.enums

import dev.alexandrevieira.manager.apiclients.bcb.dto.KeyType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoChaveTest {

    @Nested
    inner class ALEATORIA {
        @Test
        @DisplayName("Deve ser valida se nao possuir valor")
        internal fun deveSerValidaSeNaoPossuirValor() {
            with(TipoChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
                assertTrue(valida(" "))
            }
        }

        @Test
        @DisplayName("Deve ser invalida se chave possuir valor")
        internal fun deveSerInvalidaSeChavePossuirValor() {
            with(TipoChave.ALEATORIA) {
                assertFalse(valida("algum valor"))
            }
        }

        @Test
        @DisplayName("Deve converter o tipo para BCB")
        internal fun deveConverterTipoParaBcb() {
            val keyType: KeyType = TipoChave.ALEATORIA.converte()
            assertEquals(KeyType.RANDOM, keyType)
        }
    }

    @Nested
    inner class CPF {
        @Test
        @DisplayName("Deve ser valida se CPF valido")
        internal fun deveSerValidaSeCpfValido() {
            with(TipoChave.CPF) {
                assertTrue(valida("05982564079"))
                assertTrue(valida("21115799045"))
                assertTrue(valida("63198340055"))
            }
        }

        @Test
        @DisplayName("Deve ser invalida se CPF invalido")
        internal fun deveSerInvalidaSeCpfInvalido() {
            with(TipoChave.CPF) {
                assertFalse(valida("12345678900"))
                assertFalse(valida("12312312312"))
            }
        }

        @Test
        @DisplayName("Deve ser invalida se CPF em formato invalido")
        internal fun deveSerInvalidaSeCpfEmFormatoInvalido() {
            with(TipoChave.CPF) {
                assertFalse(valida("059.825.640-79"))
                assertFalse(valida("+5534991999177"))
                assertFalse(valida("email@email.com"))
                assertFalse(valida(" "))
                assertFalse(valida(null))
            }
        }

        @Test
        @DisplayName("Deve converter o tipo para BCB")
        internal fun deveConverterTipoParaBcb() {
            val keyType: KeyType = TipoChave.CPF.converte()
            assertEquals(KeyType.CPF, keyType)
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        @DisplayName("Deve ser valida se email em formato valido")
        internal fun deveSerValidaSeEmailEmFormatoValido() {
            with(TipoChave.EMAIL) {
                assertTrue(valida("email@email.com"))
                assertTrue(valida("email@email.com.it"))
                assertTrue(valida("email@us.gov"))
            }
        }

        @Test
        @DisplayName("Deve ser invalida se email em formato invalido")
        internal fun deveSerInvalidaSeEmailEmFormatoInvalido() {
            with(TipoChave.EMAIL) {
                assertFalse(valida("email@email"))
                assertFalse(valida("email@email.c"))
                assertFalse(valida("email@co"))
                assertFalse(valida("12345678900"))
                assertFalse(valida("+5534991979197"))
                assertFalse(valida(" "))
                assertFalse(valida(null))
            }
        }

        @Test
        @DisplayName("Deve converter o tipo para BCB")
        internal fun deveConverterTipoParaBcb() {
            val keyType: KeyType = TipoChave.EMAIL.converte()
            assertEquals(KeyType.EMAIL, keyType)
        }
    }

    @Nested
    inner class CELULAR {
        @Test
        @DisplayName("Deve ser valida se celular em formato valido")
        internal fun deveSerValidaSeCelularEmFormatoValido() {
            with(TipoChave.CELULAR) {
                assertTrue(valida("+5534991779177"))
                assertTrue(valida("+5511984305543"))
                assertTrue(valida("+12125557385"))
            }
        }

        @Test
        @DisplayName("Deve ser invalida se email em formato invalido")
        internal fun deveSerInvalidaSeEmailEmFormatoInvalido() {
            with(TipoChave.CELULAR) {
                assertFalse(valida("+55"))
                assertFalse(valida("034991779177"))
                assertFalse(valida("5534991977155"))
                assertFalse(valida("12345678900"))
                assertFalse(valida("email@email.com"))
                assertFalse(valida(null))
            }
        }

        @Test
        @DisplayName("Deve converter o tipo para BCB")
        internal fun deveConverterTipoParaBcb() {
            val keyType: KeyType = TipoChave.CELULAR.converte()
            assertEquals(KeyType.PHONE, keyType)
        }
    }
}