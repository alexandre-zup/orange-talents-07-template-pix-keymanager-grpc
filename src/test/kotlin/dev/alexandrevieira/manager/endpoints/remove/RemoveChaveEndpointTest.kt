package dev.alexandrevieira.manager.endpoints.remove

import dev.alexandrevieira.manager.apiclients.bcb.BcbClient
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyRequest
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyResponse
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
import dev.alexandrevieira.stubs.KeyManagerRemoveServiceGrpc
import dev.alexandrevieira.stubs.RemoveChaveRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest {
    @field:Inject
    private lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    private lateinit var instituicaoRepository: InstituicaoRepository

    @field:Inject
    private lateinit var contaRepository: ContaRepository

    @field:Inject
    private lateinit var titularRepository: TitularRepository

    @field:Inject
    private lateinit var keyManager: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var bcbClient: BcbClient

    @BeforeEach
    fun setUp() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll()
        titularRepository.deleteAll()
        instituicaoRepository.deleteAll()
    }

    @Test
    @DisplayName("Deve remover uma chave pix")
    fun deveRemoverUmaChavePix() {
        assertEquals(0, chavePixRepository.count())
        val chave = chavePixRepository.save(chaveFactory())
        assertEquals(1, chavePixRepository.count())

        val bcbRequest = BcbDeletePixKeyRequest(chave.chave, chave.conta.instituicao.ispb)
        val bcbResponse = BcbDeletePixKeyResponse(bcbRequest.key, bcbRequest.participant, LocalDateTime.now())
        Mockito.`when`(bcbClient.remove(bcbRequest.key, bcbRequest))
            .thenReturn(HttpResponse.ok(bcbResponse))

        keyManager.remove(
            RemoveChaveRequest.newBuilder()
                .setChavePixId(chave.id.toString())
                .setClienteId(chave.obterTitularId().toString())
                .build()
        )
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    @DisplayName("Deve dar erro ao tentar remover uma chave inexistente")
    fun deveDarErroAoTentarRemoverUmaChaveInexistente() {
        assertEquals(0, chavePixRepository.count())
        val chave = chaveFactory()
        val erro = assertThrows<StatusRuntimeException> {
            keyManager.remove(
                RemoveChaveRequest.newBuilder()
                    .setChavePixId(UUID.randomUUID().toString())
                    .setClienteId(chave.obterTitularId().toString())
                    .build()
            )
        }
        assertEquals(0, chavePixRepository.count())
        assertNotNull(erro)
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve dar erro ao tentar remover uma chave de outro cliente")
    fun deveDarErroAoTentarRemoverUmaChaveDeOutroCliente() {
        assertEquals(0, chavePixRepository.count())
        val chave = chavePixRepository.save(chaveFactory())
        assertEquals(1, chavePixRepository.count())
        val erro = assertThrows<StatusRuntimeException> {
            keyManager.remove(
                RemoveChaveRequest.newBuilder()
                    .setChavePixId(chave.id.toString())
                    .setClienteId(UUID.randomUUID().toString()) //mandando um id de um cliente diferente (inexistente)
                    .build()
            )
        }

        assertEquals(1, chavePixRepository.count())
        assertNotNull(erro)
        assertEquals(Status.PERMISSION_DENIED.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve dar erro ao tentar remover uma chave de outra instituicao")
    fun deveDarErroAoTentarRemoverUmaChaveDeOutraInstituicao() {
        assertEquals(0, chavePixRepository.count())
        val chave = chavePixRepository.save(chaveFactory())
        assertEquals(1, chavePixRepository.count())

        val bcbRequest = BcbDeletePixKeyRequest(chave.chave, chave.conta.instituicao.ispb)

        Mockito.`when`(bcbClient.remove(chave.chave, bcbRequest))
            .thenThrow(HttpClientResponseException("", HttpResponse.status<Void>(HttpStatus.FORBIDDEN)))

        val erro = assertThrows<StatusRuntimeException> {
            keyManager.remove(
                RemoveChaveRequest.newBuilder()
                    .setChavePixId(chave.id.toString())
                    .setClienteId(chave.obterTitularId().toString())
                    .build()
            )
        }

        assertEquals(1, chavePixRepository.count())
        assertNotNull(erro)
        assertEquals(Status.PERMISSION_DENIED.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve simular resposta inesperada do BCB")
    fun deveSimularRespostaInesperadaDoBcb() {
        assertEquals(0, chavePixRepository.count())
        val chave = chavePixRepository.save(chaveFactory())
        assertEquals(1, chavePixRepository.count())

        val bcbRequest = BcbDeletePixKeyRequest(chave.chave, chave.conta.instituicao.ispb)

        Mockito.`when`(bcbClient.remove(chave.chave, bcbRequest))
            .thenThrow(HttpClientResponseException("", HttpResponse.badRequest<Void>()))

        val erro = assertThrows<StatusRuntimeException> {
            keyManager.remove(
                RemoveChaveRequest.newBuilder()
                    .setChavePixId(chave.id.toString())
                    .setClienteId(chave.obterTitularId().toString())
                    .build()
            )
        }

        assertEquals(1, chavePixRepository.count())
        assertNotNull(erro)
        assertEquals(Status.INTERNAL.code, erro.status.code)

    }

    @Test
    @DisplayName("Deve simular BCB caido")
    fun deveSimularBcbCaido() {
        assertEquals(0, chavePixRepository.count())
        val chave = chavePixRepository.save(chaveFactory())
        assertEquals(1, chavePixRepository.count())

        val bcbRequest = BcbDeletePixKeyRequest(chave.chave, chave.conta.instituicao.ispb)

        Mockito.`when`(bcbClient.remove(chave.chave, bcbRequest))
            .thenThrow(HttpClientException("Cannot connect"))

        val erro = assertThrows<StatusRuntimeException> {
            keyManager.remove(
                RemoveChaveRequest.newBuilder()
                    .setChavePixId(chave.id.toString())
                    .setClienteId(chave.obterTitularId().toString())
                    .build()
            )
        }

        assertEquals(1, chavePixRepository.count())
        assertNotNull(erro)
        assertEquals(Status.UNAVAILABLE.code, erro.status.code)
    }

    fun chaveFactory(
        instituicao: Instituicao = Instituicao("Itau", "12345678"),
        titular: Titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097"),
        conta: Conta = Conta("0001", "12345", TipoConta.CONTA_CORRENTE, titular, instituicao)
    ): ChavePix {
        return ChavePix(conta, UUID.randomUUID().toString(), TipoChave.ALEATORIA)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub {
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}