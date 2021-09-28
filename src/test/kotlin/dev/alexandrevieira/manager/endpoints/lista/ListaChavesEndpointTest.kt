package dev.alexandrevieira.manager.endpoints.lista

import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.model.Conta
import dev.alexandrevieira.manager.data.model.Instituicao
import dev.alexandrevieira.manager.data.model.Titular
import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.data.repositories.ContaRepository
import dev.alexandrevieira.manager.data.repositories.InstituicaoRepository
import dev.alexandrevieira.manager.data.repositories.TitularRepository
import dev.alexandrevieira.stubs.KeyManagerListaServiceGrpc
import dev.alexandrevieira.stubs.ListaChaveRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest {
    @field:Inject
    private lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    private lateinit var instituicaoRepository: InstituicaoRepository

    @field:Inject
    private lateinit var contaRepository: ContaRepository

    @field:Inject
    private lateinit var titularRepository: TitularRepository

    @field:Inject
    private lateinit var keyManagerClient: KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub

    private val instituicao: Instituicao = Instituicao("Itau", "12345678")

    @AfterEach
    fun tearDown() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll()
        titularRepository.deleteAll()
        instituicaoRepository.deleteAll()
    }

    @Test
    @DisplayName("Deve listar chaves de um cliente")
    internal fun deveListarChavesDeUmCliente() {
        assertEquals(0, chavePixRepository.count())
        val titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097")
        val poupanca = Conta("0001", "12345", TipoConta.CONTA_POUPANCA, titular, instituicao)
        val aleatoriaPoupanca = chaveAleatoria(titular = titular, instituicao = instituicao, conta = poupanca)
        chavePixRepository.save(aleatoriaPoupanca)

        val corrente = Conta("0001", "12345", TipoConta.CONTA_CORRENTE, titular, instituicao)
        val emailCorrente = chaveEmail(titular = titular, instituicao = instituicao, conta = corrente)

        chavePixRepository.update(emailCorrente)

        assertEquals(2, chavePixRepository.count())

        val lista = keyManagerClient.lista(
            ListaChaveRequest.newBuilder()
                .setClienteId(titular.id.toString())
                .build()
        )

        assertEquals(2, lista.chavesList.size)

        assertEquals(aleatoriaPoupanca.chave, lista.chavesList[0].chave)
        assertEquals(aleatoriaPoupanca.tipo.name, lista.chavesList[0].tipo.name)
        assertEquals(aleatoriaPoupanca.conta.tipo.name, lista.chavesList[0].tipoConta.name)

        assertEquals(emailCorrente.chave, lista.chavesList[1].chave)
        assertEquals(emailCorrente.tipo.name, lista.chavesList[1].tipo.name)
        assertEquals(emailCorrente.conta.tipo.name, lista.chavesList[1].tipoConta.name)
    }

    @Test
    @DisplayName("Deve retornar lista vazia se nao houver chave")
    internal fun deveRetornarListaVaziaSeNaoHouverChave() {
        assertEquals(0, chavePixRepository.count())
        val titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097")
        val poupanca = Conta("0001", "12345", TipoConta.CONTA_POUPANCA, titular, instituicao)
        val aleatoriaPoupanca = chaveAleatoria(titular = titular, instituicao = instituicao, conta = poupanca)
        chavePixRepository.save(aleatoriaPoupanca)

        assertEquals(1, chavePixRepository.count())

        val lista = keyManagerClient.lista(
            ListaChaveRequest.newBuilder()
                .setClienteId(UUID.randomUUID().toString()) //buscando cliente diferente da chave criada
                .build()
        )

        assertEquals(0, lista.chavesList.size)
    }

    @Test
    @DisplayName("Deve falhar ao tentar um id invalido")
    internal fun deveFalharAoTentarUmIdInvalido() {
        //buscando cliente com id invalido
        val request1 = ListaChaveRequest.newBuilder().setClienteId("abd123").build()
        val erro1 = assertThrows<StatusRuntimeException> { keyManagerClient.lista(request1) }

        //buscando com id em branco
        val request2 = ListaChaveRequest.newBuilder().setClienteId(" ").build()
        val erro2 = assertThrows<StatusRuntimeException> { keyManagerClient.lista(request2) }


        assertNotNull(erro1)
        assertEquals(Status.INVALID_ARGUMENT.code, erro1.status.code)

        assertNotNull(erro2)
        assertEquals(Status.INVALID_ARGUMENT.code, erro2.status.code)
    }


    private fun chaveAleatoria(
        instituicao: Instituicao = Instituicao("Itau", "12345678"),
        titular: Titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097"),
        conta: Conta = Conta("0001", "12345", TipoConta.CONTA_POUPANCA, titular, instituicao)
    ): ChavePix {
        val chave = ChavePix(conta, "", TipoChave.ALEATORIA)
        chave.informaCriacaoNoBcb(UUID.randomUUID().toString(), LocalDateTime.now())
        return chave
    }

    private fun chaveEmail(
        instituicao: Instituicao = Instituicao("Itau", "12345678"),
        titular: Titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097"),
        conta: Conta = Conta("0001", "12345", TipoConta.CONTA_CORRENTE, titular, instituicao)
    ): ChavePix {
        val chave = ChavePix(conta, "meu@email.com", TipoChave.EMAIL)
        chave.informaCriacaoNoBcb(chave.chave, LocalDateTime.now())
        return chave
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub? {
            return KeyManagerListaServiceGrpc.newBlockingStub(channel)
        }
    }
}