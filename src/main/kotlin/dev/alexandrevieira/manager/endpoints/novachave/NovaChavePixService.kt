package dev.alexandrevieira.manager.endpoints.novachave

import dev.alexandrevieira.manager.apiclients.erpitau.ErpItauClient
import dev.alexandrevieira.manager.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.manager.apiclients.erpitau.ConverterService
import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.customexceptions.ChavePixExistenteException
import dev.alexandrevieira.manager.exception.customexceptions.InternalServerError
import dev.alexandrevieira.manager.exception.customexceptions.ServiceUnavailableException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
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