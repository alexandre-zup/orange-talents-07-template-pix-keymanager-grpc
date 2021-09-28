package dev.alexandrevieira.manager.endpoints.consulta

import dev.alexandrevieira.manager.apiclients.bcb.BcbClient
import dev.alexandrevieira.manager.apiclients.bcb.dto.*
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
import dev.alexandrevieira.stubs.ConsultaChaveRequest
import dev.alexandrevieira.stubs.ConsultaChaveResponse
import dev.alexandrevieira.stubs.KeyManagerConsultaServiceGrpc
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
internal class ConsultaChaveEndpointTest {
    @field:Inject
    private lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    private lateinit var instituicaoRepository: InstituicaoRepository

    @field:Inject
    private lateinit var contaRepository: ContaRepository

    @field:Inject
    private lateinit var titularRepository: TitularRepository

    @field:Inject
    @field:Client("/")
    lateinit var bcbClient: BcbClient

    @field:Inject
    lateinit var keyManagerClient: KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub

    @AfterEach
    fun tearDown() {
        chavePixRepository.deleteAll()
        contaRepository.deleteAll()
        titularRepository.deleteAll()
        instituicaoRepository.deleteAll()
    }

    @Test
    @DisplayName("Deve consultar por PixId")
    internal fun deveConsultarPorPixId() {
        assertEquals(0, chavePixRepository.count())
        val chave = chaveEmail()
        assertEquals(1, chavePixRepository.count())

        val porIdBuilder = ConsultaChaveRequest.FiltroPorId.newBuilder()
            .setChavePixId(chave.id.toString())
            .setClienteId(chave.obterTitularId().toString())


        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setPixId(porIdBuilder)
            .build()

        val grpcResponse: ConsultaChaveResponse = keyManagerClient.consulta(grpcRequest)

        assertNotNull(grpcResponse)
        assertTrue(chave.criadaNoBcb)
        assertEquals(chave.chave, grpcResponse.chave)
        assertEquals(chave.tipo.name, grpcResponse.tipo.name)
        assertEquals(chave.conta.numero, grpcResponse.conta.numero)
        assertEquals(chave.conta.titular.cpf, grpcResponse.conta.titular.cpf)
        assertEquals(chave.conta.instituicao.ispb, grpcResponse.conta.instituicao.ispb)
    }

    @Test
    @DisplayName("Deve falhar para ClienteId diferente")
    internal fun deveFalharParaClienteIdDIferente() {
        assertEquals(0, chavePixRepository.count())
        val chave = chaveEmail()
        assertEquals(1, chavePixRepository.count())

        val porIdBuilder = ConsultaChaveRequest.FiltroPorId.newBuilder()
            .setChavePixId(chave.id.toString())
            .setClienteId(UUID.randomUUID().toString())


        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setPixId(porIdBuilder)
            .build()

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.consulta(grpcRequest) }

        assertNotNull(erro)
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve simular BCB caido")
    internal fun deveSimularBcbCaido() {
        Mockito.`when`(bcbClient.consulta("92393978097"))
            .thenThrow(HttpClientException("Cannot connect"))

        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setChave("92393978097")
            .build()

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.consulta(grpcRequest) }

        assertNotNull(erro)
        assertEquals(Status.UNAVAILABLE.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve simular Bad Request do BCB")
    internal fun deveSimularBadRequestDoBcb() {
        //não deveria acontecer um Bad Request, pois validamos antes, sendo assim seria um erro 500
        Mockito.`when`(bcbClient.consulta("92393978097"))
            .thenThrow(HttpClientResponseException("Bad request", HttpResponse.badRequest<Void>()))

        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setChave("92393978097")
            .build()

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.consulta(grpcRequest) }

        assertNotNull(erro)
        assertEquals(Status.INTERNAL.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve falhar para PixId inexistente")
    internal fun deveFalharParaPixIdInexistente() {
        val porIdBuilder = ConsultaChaveRequest.FiltroPorId.newBuilder()
            .setChavePixId(UUID.randomUUID().toString())
            .setClienteId(UUID.randomUUID().toString())

        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setPixId(porIdBuilder)
            .build()

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.consulta(grpcRequest) }

        assertNotNull(erro)
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }


    @Test
    @DisplayName("Deve consultar por chave que existe no repository")
    internal fun deveConsultarPorChaveQueExisteNoRepository() {
        assertEquals(0, chavePixRepository.count())
        val chave = chaveAleatoria()
        assertEquals(1, chavePixRepository.count())

        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setChave(chave.chave)
            .build()

        val grpcResponse: ConsultaChaveResponse = keyManagerClient.consulta(grpcRequest)

        assertNotNull(grpcResponse)
        assertTrue(chave.criadaNoBcb)
        assertEquals(UUID.fromString(chave.chave), UUID.fromString(grpcResponse.chave))
        assertEquals(chave.tipo.name, grpcResponse.tipo.name)
        assertEquals(chave.conta.numero, grpcResponse.conta.numero)
        assertEquals(chave.conta.titular.cpf, grpcResponse.conta.titular.cpf)
        assertEquals(chave.conta.instituicao.ispb, grpcResponse.conta.instituicao.ispb)
    }

    @Test
    @DisplayName("Deve consultar por chave que so existe no BCB")
    internal fun deveConsultarPorChaveQueSoExiteNoBcb() {
        val bcbResponse: BcbPixKeyResponse = bcbResponse()
        val chave: String = bcbResponse.key
        Mockito.`when`(bcbClient.consulta(chave))
            .thenReturn(HttpResponse.ok(bcbResponse))


        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setChave(bcbResponse.key)
            .build()

        val grpcResponse: ConsultaChaveResponse = keyManagerClient.consulta(grpcRequest)

        assertNotNull(grpcResponse)
        assertEquals(chave, grpcResponse.chave)
    }

    @Test
    @DisplayName("Deve falhar ao consultar uma chave que nao existe localmente nem no BCB")
    internal fun deveFalharAoConsultarPorChaveQueNaoExisteLocalNemNoBcb() {
        val chave = "98865416017"
        Mockito.`when`(bcbClient.consulta(chave))
            .thenReturn(HttpResponse.notFound())


        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .setChave(chave)
            .build()

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.consulta(grpcRequest) }

        assertNotNull(erro)
        assertEquals(Status.NOT_FOUND.code, erro.status.code)
    }

    @Test
    @DisplayName("Deve falhar ao fazer uma consulta sem filtro definido")
    internal fun deveFalharAoFazerUmaConsultaSemFiltroDefinido() {
        val grpcRequest = ConsultaChaveRequest.newBuilder()
            .build()

        val erro = assertThrows<StatusRuntimeException> { keyManagerClient.consulta(grpcRequest) }

        assertNotNull(erro)
        assertEquals(Status.INVALID_ARGUMENT.code, erro.status.code)
    }


    private fun chaveAleatoria(
        instituicao: Instituicao = Instituicao("Itau", "12345678"),
        titular: Titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097"),
        conta: Conta = Conta("0001", "12345", TipoConta.CONTA_CORRENTE, titular, instituicao)
    ): ChavePix {
        return ChavePix(conta, "", TipoChave.ALEATORIA).let {
            it.informaCriacaoNoBcb(UUID.randomUUID().toString(), LocalDateTime.now())
            chavePixRepository.save(it)
        }
    }

    private fun chaveEmail(
        instituicao: Instituicao = Instituicao("Itau", "12345678"),
        titular: Titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097"),
        conta: Conta = Conta("0001", "12345", TipoConta.CONTA_CORRENTE, titular, instituicao)
    ): ChavePix {
        return ChavePix(conta, "meu@email.com", TipoChave.EMAIL).let {
            it.informaCriacaoNoBcb(it.chave, LocalDateTime.now())
            chavePixRepository.save(it)
        }
    }

    private fun bcbResponse(): BcbPixKeyResponse {
        val owner = OwnerDTO(PersonType.NATURAL_PERSON, "Alexandre", "98865416017")
        val account = BankAccountDTO("60701190", "0001", "123455", AccountType.CACC)
        return BcbPixKeyResponse(KeyType.RANDOM, UUID.randomUUID().toString(), account, owner, LocalDateTime.now())
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub? {
            return KeyManagerConsultaServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}