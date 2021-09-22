package dev.alexandrevieira.endpoints.novachave

import dev.alexandrevieira.ErpItauClient
import dev.alexandrevieira.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.apiclients.erpitau.ConverterService
import dev.alexandrevieira.data.model.ChavePix
import dev.alexandrevieira.data.repositories.ChavePixRepository
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService {
    private val log = LoggerFactory.getLogger(this.javaClass)
    @Inject
    lateinit var validator: Validator
    @Inject
    lateinit var repository: ChavePixRepository
    @Inject
    lateinit var itauClient: ErpItauClient
    @Inject
    lateinit var converterService: ConverterService

    @Transactional
    fun registra(@Valid novaChave: NovaChavePixValidated): ChavePix {
//        if (repository.existsByChave(novaChave.chave!!))
//            throw ChavePixExistenteException("Chave ${novaChave.chave} já existe")


        val response = itauClient.buscaConta(novaChave.clienteId!!, novaChave.tipoConta!!)
        val contaResponse: ContaResponse =
            response.body() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val conta = converterService.toModel(contaResponse)
        val chave = novaChave.toModel(conta)
        repository.save(chave)
        return chave
    }
}