package dev.alexandrevieira.endpoints.novachave

import dev.alexandrevieira.*
import dev.alexandrevieira.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.data.model.enums.TipoConta
import dev.alexandrevieira.data.repositories.ChavePixRepository
import dev.alexandrevieira.data.repositories.ContaRepository
import dev.alexandrevieira.data.repositories.InstituicaoRepository
import dev.alexandrevieira.data.repositories.TitularRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import java.util.*

@MicronautTest(transactional = false)
internal class NovaChaveEndpointTest {
    @field:Inject
    lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    lateinit var titularRepository: TitularRepository

    @field:Inject
    lateinit var instituicaoRepository: InstituicaoRepository

    @field:Inject
    lateinit var contaRepository: ContaRepository

    @field:Inject
    @field:Client("/")
    lateinit var itauClient: ErpItauClient

    @field:Inject
    lateinit var keyManagerClient: PixKeyManagerServiceGrpc.PixKeyManagerServiceBlockingStub


    @AfterEach
    internal fun tearDown() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll()
        titularRepository.deleteAll()
        instituicaoRepository.deleteAll()
    }

    @Test
    @DisplayName("Deve cadastrar uma chave pix CPF")
    internal fun deveCadastrarChavePixCpf() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.CPF)
            .setValorChave("02467781054")
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val response: NovaChavePixResponse = keyManagerClient.registra(request)

        with(response) {
            assertEquals(1, chavePixRepository.count())
            assertNotNull(this)
            assertNotNull(chavePixId)
            assertFalse(chavePixId.isNullOrEmpty())
            assertDoesNotThrow { UUID.fromString(chavePixId) }
            assertEquals(request.clienteId, this.clienteId)
        }
    }

    @Test
    @DisplayName("Nao deve permitir chaves repetidas")
    internal fun naoDevePermitirChavesRepetidas() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.CPF)
            .setValorChave("02467781054")
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val response: NovaChavePixResponse = keyManagerClient.registra(request)
        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        with(response) {
            assertNotNull(this)
            assertNotNull(this.chavePixId)
            assertFalse(this.chavePixId.isNullOrEmpty())
            assertEquals(request.clienteId, this.clienteId)
        }

        assertEquals(1, chavePixRepository.count())
        assertEquals(Status.Code.ALREADY_EXISTS, erro.status.code)
    }

    @Test
    @DisplayName("Nao deve permitir CPF invalido")
    internal fun naoDevePermitirCpfInvalido() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.CPF)
            .setValorChave("12345678900")
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(Status.Code.INVALID_ARGUMENT, erro.status.code)
        assertEquals(0, chavePixRepository.count())
    }

    @Test
    @DisplayName("Deve lancar excecao caso o ERP estaja caido")
    internal fun deveLancarExcecaoCasoErpEstejaCaido() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("35f97e0a-107c-41c9-adc4-1fb894ab76e3")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenThrow(HttpClientException(""))

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.UNAVAILABLE, erro.status.code)

    }

    @Test
    @DisplayName("Deve permitir segunda chave para a mesma conta")
    internal fun devePermitirSegundaChaveParaMesmaConta() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val response1: NovaChavePixResponse = keyManagerClient.registra(request)
        val response2: NovaChavePixResponse = keyManagerClient.registra(request)


        assertEquals(2, chavePixRepository.count())
        assertEquals(1, contaRepository.count())
        assertEquals(1, titularRepository.count())
        assertEquals(1, contaRepository.count())

        with(response1) {
            assertNotNull(this)
            assertNotNull(chavePixId)
            assertFalse(chavePixId.isNullOrEmpty())
            assertDoesNotThrow { UUID.fromString(chavePixId) }
            assertEquals(request.clienteId, this.clienteId)
        }

        with(response2) {
            assertNotNull(this)
            assertNotNull(chavePixId)
            assertFalse(chavePixId.isNullOrEmpty())
            assertDoesNotThrow { UUID.fromString(chavePixId) }
            assertEquals(request.clienteId, this.clienteId)
        }

    }

    @Test
    @DisplayName("Deve simular resposta inesperada do ERP Itau")
    internal fun deveSimularRespostaInesperadaDoItau() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("36c24af4-35c7-4f8b-a155-702a38c4459e")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_POUPANCA)
            .build()

        //Está retornando status 400 Bad request, mas nesse caso deveria lancar uma excecao
        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.badRequest())

        val error = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.INTERNAL, error.status.code)
    }

    @Test
    @DisplayName("Deve lancar excecao caso o cliente nao exista no Itau")
    internal fun deveLancarExcecaoCasoClienteNaoExistaNoItau() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("58da5c9a-e1a4-4b95-95c5-af212d9da6f7")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_POUPANCA)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.FAILED_PRECONDITION, error.status.code)
    }

    private fun contaResponse(): ContaResponse {
        val instituicao = ContaResponse.InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190")
        val titular = ContaResponse.TitularResponse(
            UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            "Rafael M C Ponte",
            "02467781054"
        )
        return ContaResponse("CONTA_CORRENTE", instituicao, "0001", "291900", titular)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyManagerServiceGrpc.PixKeyManagerServiceBlockingStub? {
            return PixKeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ErpItauClient::class)
    fun itauClient(): ErpItauClient {
        return Mockito.mock(ErpItauClient::class.java)
    }
}