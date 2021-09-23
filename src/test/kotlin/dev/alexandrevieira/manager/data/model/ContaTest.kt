package dev.alexandrevieira.manager.data.model

import dev.alexandrevieira.manager.data.model.enums.TipoConta.CONTA_CORRENTE
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

internal class ContaTest {

    @Test
    @DisplayName("Testa properties")
    internal fun testaProperties() {
        val iNome = "Itau"
        val ispb = "12345678"
        val instituicao = Instituicao(iNome, ispb)

        val tNome = "Joao"
        val cpf = "12345678900"
        val id = UUID.randomUUID()
        val titular = Titular(id, tNome, cpf)

        val agencia = "0001"
        val cNumero = "12345"
        val conta = Conta(agencia, cNumero, CONTA_CORRENTE, titular, instituicao)

        assertNotNull(instituicao)
        assertNull(instituicao.id)
        assertEquals(iNome, instituicao.nome)
        assertEquals(ispb, instituicao.ispb)

        assertNotNull(titular)
        assertEquals(tNome, titular.nome)
        assertEquals(cpf, titular.cpf)
        assertEquals(id, titular.id)

        assertNotNull(conta)
        assertNull(conta.id)
        assertEquals(instituicao, conta.instituicao)
        assertEquals(titular, conta.titular)
        assertEquals(agencia, conta.agencia)
        assertEquals(cNumero, conta.numero)
        assertEquals(CONTA_CORRENTE, conta.tipo)
    }
}