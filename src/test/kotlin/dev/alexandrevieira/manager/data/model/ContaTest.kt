package dev.alexandrevieira.manager.data.model

import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.data.repositories.ContaRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ContaTest {
    @Inject
    lateinit var repository: ContaRepository


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
        val conta = Conta(agencia, cNumero, TipoConta.CONTA_CORRENTE, titular, instituicao)
        repository.save(conta)

        assertNotNull(conta.id)
    }
}