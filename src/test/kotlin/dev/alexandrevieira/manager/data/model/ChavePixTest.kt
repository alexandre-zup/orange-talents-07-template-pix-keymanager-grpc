package dev.alexandrevieira.manager.data.model

import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = true)
internal class ChavePixTest {
    @Inject
    lateinit var chaveRepository: ChavePixRepository
    lateinit var conta: Conta

    @BeforeEach
    internal fun setUp() {
        chaveRepository.deleteAll()
        val iNome = "Itau"
        val ispb = "12345678"
        val instituicao = Instituicao(iNome, ispb)

        val tNome = "Joao"
        val cpf = "12345678900"
        val id = UUID.randomUUID()
        val titular = Titular(id, tNome, cpf)

        val agencia = "0001"
        val cNumero = "12345"
        conta = Conta(agencia, cNumero, TipoConta.CONTA_CORRENTE, titular, instituicao)
    }

    @Test
    @DisplayName("Deve atualizar chave aleatoria")
    fun deveAtualizarChaveAleatoria() {
        val chave: ChavePix = ChavePix(conta, "", TipoChave.ALEATORIA)
        chaveRepository.save(chave)
        val criadaEm: LocalDateTime = chave.criadaEm
        val criadaNoBcb: Boolean = chave.criadaNoBcb
        val valorChave: String = chave.chave

        chave.informaCriacaoNoBcb(UUID.randomUUID().toString(), criadaEm.plusSeconds(1))

        assertFalse(criadaNoBcb)
        assertTrue(chave.criadaNoBcb)
        assertNotEquals(valorChave, chave.chave)
        assertNotEquals(criadaEm, chave.criadaEm)
    }

    @Test
    @DisplayName("Deve atualizar chave diferente de aleatoria")
    fun naoDeveAtualizarChaveDiferenteDeAleatoria() {
        val chave: ChavePix = ChavePix(conta, "meu@email.com", TipoChave.EMAIL)
        chaveRepository.save(chave)
        val criadaEm: LocalDateTime = chave.criadaEm
        val criadaNoBcb: Boolean = chave.criadaNoBcb
        val valorChave: String = chave.chave

        chave.informaCriacaoNoBcb("qualquer chave", LocalDateTime.now())

        assertFalse(criadaNoBcb)
        assertTrue(chave.criadaNoBcb)
        assertEquals(valorChave, chave.chave)
        assertNotEquals(criadaEm, chave.criadaEm)
    }
}