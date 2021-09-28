package dev.alexandrevieira.manager.endpoints.lista

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
import dev.alexandrevieira.stubs.KeyManagerListaServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
    @field:Client("/")
    lateinit var bcbClient: BcbClient

    @field:Inject
    lateinit var keyManagerClient: KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub

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
//        Mockito.`when`()
//        val aleatoriaPoupanca = chaveAleatoria()
//        val emailCorrente = chaveEmail()



    }


    private fun chaveAleatoria(
        instituicao: Instituicao = Instituicao("Itau", "12345678"),
        titular: Titular = Titular(UUID.randomUUID(), "Alexandre", "92393978097"),
        conta: Conta = Conta("0001", "12345", TipoConta.CONTA_POUPANCA, titular, instituicao)
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
                KeyManagerListaServiceGrpc.KeyManagerListaServiceBlockingStub? {
            return KeyManagerListaServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
    }
}