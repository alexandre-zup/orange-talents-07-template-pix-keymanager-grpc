package dev.alexandrevieira.manager.endpoints.novachave

import dev.alexandrevieira.manager.apiclients.bcb.*
import dev.alexandrevieira.manager.apiclients.bcb.dto.*
import dev.alexandrevieira.manager.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.manager.apiclients.erpitau.ErpClient
import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.data.repositories.ContaRepository
import dev.alexandrevieira.manager.data.repositories.InstituicaoRepository
import dev.alexandrevieira.manager.data.repositories.TitularRepository
import dev.alexandrevieira.stubs.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
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
    lateinit var itauClient: ErpClient

    @field:Inject
    @field:Client("/")
    lateinit var bcbClient: BcbClient

    @field:Inject
    lateinit var keyManagerClient: KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub


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

        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse(bcbRequest)))

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

        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse(bcbRequest)))

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

        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse(bcbRequest)))

        val response1: NovaChavePixResponse = keyManagerClient.registra(request)

        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenReturn(HttpResponse.created(bcbResponse(bcbRequest)))
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

    @Test
    @DisplayName("Deve simular chave ja cadastrada no BCB")
    internal fun deveSimularChaveJaCadastradaNoBcb() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenThrow(HttpClientResponseException("Chave já existe", HttpResponse.unprocessableEntity<Void>()))

        val error = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.FAILED_PRECONDITION, error.status.code)
    }

    @Test
    @DisplayName("Deve simular uma resposta inesperada do BCB")
    internal fun deveSimularRespostaInesperadaDoBcb() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenReturn(HttpResponse.ok(bcbResponse(bcbRequest)))

        val error = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.INTERNAL, error.status.code)
    }

    @Test
    @DisplayName("Deve simular bad request do BCB")
    internal fun deveSimularBadRequestDoBcb() {
        //Simula uma situação de erro que não deveria acontecer
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        //não deveria chegar nesse ponto, pois os dados já precisam estar validados antes de enviar a reqeust
        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenThrow(HttpClientResponseException("Requisição inválida", HttpResponse.badRequest<Void>()))

        val error = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.INTERNAL, error.status.code)
    }

    @Test
    @DisplayName("Deve lancar excecao caso o BCB estaja caido")
    internal fun deveLancarExcecaoCasoBcbEstejaCaido() {
        assertEquals(0, chavePixRepository.count())

        val request: NovaChavePixRequest = NovaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipoChave(TipoDaChave.ALEATORIA)
            .setTipoConta(TipoDaConta.CONTA_CORRENTE)
            .build()

        Mockito.`when`(itauClient.buscaConta(request.clienteId, TipoConta.valueOf(request.tipoConta.name)))
            .thenReturn(HttpResponse.ok(contaResponse()))

        val bcbRequest = bcbRequest(TipoChave.valueOf(request.tipoChave.name), contaResponse(), request.valorChave)
        Mockito.`when`(bcbClient.registra(bcbRequest))
            .thenThrow(HttpClientException("Erro de conexão"))

        val error = assertThrows<StatusRuntimeException> { keyManagerClient.registra(request) }

        assertEquals(0, chavePixRepository.count())
        assertEquals(Status.Code.UNAVAILABLE, error.status.code)

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

    private fun bcbRequest(
        tipoChave: TipoChave,
        contaResponse: ContaResponse,
        valorChave: String
    ): BcbCreatePixKeyRequest {
        val keyType: KeyType = when (tipoChave) {
            TipoChave.CPF -> KeyType.CPF
            TipoChave.CELULAR -> KeyType.PHONE
            TipoChave.EMAIL -> KeyType.EMAIL
            TipoChave.ALEATORIA -> KeyType.RANDOM
        }

        val accountType: AccountType = when (TipoConta.valueOf(contaResponse.tipo)) {
            TipoConta.CONTA_CORRENTE -> AccountType.CACC
            TipoConta.CONTA_POUPANCA -> AccountType.SVGS
        }

        val bankAccount = BankAccountDTO(
            contaResponse.instituicao.ispb,
            contaResponse.agencia,
            contaResponse.numero,
            accountType
        )

        val owner = OwnerDTO(
            PersonType.NATURAL_PERSON,
            contaResponse.titular.nome,
            contaResponse.titular.cpf
        )

        return BcbCreatePixKeyRequest(keyType, valorChave, bankAccount, owner)
    }

    private fun bcbResponse(bcbRequest: BcbCreatePixKeyRequest): BcbPixKeyResponse {
        return BcbPixKeyResponse(
            keyType = bcbRequest.keyType,
            key = if (bcbRequest.keyType == KeyType.RANDOM) UUID.randomUUID().toString() else bcbRequest.key,
            bankAccount = bcbRequest.bankAccount,
            owner = bcbRequest.owner,
            createdAt = LocalDateTime.now()
        )
    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceBlockingStub? {
            return KeyManagerRegistraServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ErpClient::class)
    fun itauClient(): ErpClient {
        return Mockito.mock(ErpClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}