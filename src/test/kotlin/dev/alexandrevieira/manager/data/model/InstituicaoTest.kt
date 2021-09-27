package dev.alexandrevieira.manager.data.model

import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.data.repositories.ContaRepository
import dev.alexandrevieira.manager.data.repositories.InstituicaoRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class InstituicaoTest {
    @Inject
    lateinit var repository: InstituicaoRepository


    @Test
    @DisplayName("Testa id")
    internal fun testaId() {
        val iNome = "Itau"
        val ispb = "12345678"
        val instituicao = Instituicao(iNome, ispb)

        repository.save(instituicao)

        assertNotNull(instituicao.id)
        assertEquals(iNome, instituicao.nome)
        assertEquals(ispb, instituicao.ispb)
    }
}