package dev.alexandrevieira.endpoints.novachave

import dev.alexandrevieira.ErpItauClient
import dev.alexandrevieira.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.apiclients.erpitau.ConverterService
import dev.alexandrevieira.data.model.ChavePix
import dev.alexandrevieira.data.repositories.ChavePixRepository
import dev.alexandrevieira.exception.customexceptions.ChavePixExistenteException
import dev.alexandrevieira.exception.customexceptions.InternalServerError
import dev.alexandrevieira.exception.customexceptions.ServiceUnavailableException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.client.exceptions.HttpClientException
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
        if (repository.existsByChave(novaChave.chave!!))
            throw ChavePixExistenteException("Chave ${novaChave.chave} já existe")

        val response: HttpResponse<ContaResponse>

        try {
            response = itauClient.buscaConta(novaChave.clienteId!!, novaChave.tipoConta!!)
        } catch (e: HttpClientException) {
            throw ServiceUnavailableException("Service Unavailable")
        }

        when (response.status) {
            NOT_FOUND -> throw IllegalStateException("Cliente não encontrado no Itau")
            OK -> {
            }
            else -> throw InternalServerError("Internal Server Error")
        }

        val contaResponse: ContaResponse = response.body()!!
        val conta = converterService.toModel(contaResponse)
        val chave = novaChave.toModel(conta)
        return repository.save(chave)
    }
}