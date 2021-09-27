package dev.alexandrevieira.manager.apiclients.bcb

import dev.alexandrevieira.manager.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.manager.data.model.Conta
import dev.alexandrevieira.manager.data.model.Instituicao
import dev.alexandrevieira.manager.data.model.Titular
import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

internal class BcbServiceTest {
    private val bcbService: BcbService = BcbService()
    lateinit var instituicao: Instituicao
    lateinit var titular: Titular

//    @BeforeEach
//    internal fun setUp() {
//        instituicao = Instituicao("Itau", "12345678")
//        titular = Titular(UUID.randomUUID(), "Alexandre", "12345678900")
//    }
//
//    @Test
//    @DisplayName("Deve criar request CELULAR / POUPANCA")
//    fun deveCriarRequestCelularPoupanca() {
//        val chave = "+5534991779177"
//        val conta = Conta("0001", "123455", TipoConta.CONTA_POUPANCA, titular, instituicao)
//        val request = bcbService.createRequest(TipoChave.CELULAR, conta, chave)
//
//        assertEquals(KeyType.PHONE, request.keyType)
//        assertEquals(AccountType.SVGS, request.bankAccount.accountType)
//    }
//
//    @Test
//    @DisplayName("Deve criar request CPF / CORRENTE")
//    fun deveCriarRequestCpfCorrente() {
//        val chave = "12345678900"
//        val conta = Conta("0001", "123455", TipoConta.CONTA_CORRENTE, titular, instituicao)
//        val request = bcbService.createRequest(TipoChave.CPF, conta, chave)
//
//        assertEquals(KeyType.CPF, request.keyType)
//        assertEquals(AccountType.CACC, request.bankAccount.accountType)
//    }
}