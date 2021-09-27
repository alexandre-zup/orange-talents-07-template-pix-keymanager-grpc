package dev.alexandrevieira.manager.endpoints.novachave

import dev.alexandrevieira.manager.apiclients.bcb.BcbService
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbCreatePixKeyRequest
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbPixKeyResponse
import dev.alexandrevieira.manager.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.manager.apiclients.erpitau.ErpItauService
import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.customexceptions.ChavePixExistenteException
import dev.alexandrevieira.manager.exception.customexceptions.InternalServerError
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.*
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
    private lateinit var repository: ChavePixRepository

    @Inject
    private lateinit var erpItauService: ErpItauService

    @Inject
    private lateinit var bcbService: BcbService

    @Transactional
    fun registra(@Valid novaChave: NovaChavePixValidated): ChavePix {
        log.info("chamado 'registra' com parâmetros: novaChave $novaChave")

        if (repository.existsByChave(novaChave.chave!!))
            throw ChavePixExistenteException("Chave ${novaChave.chave} já existe")

        log.info("Iniciando consulta no ERP Itau")

        val itauHttpResponse: HttpResponse<ContaResponse> =
            erpItauService.buscaConta(novaChave.clienteId!!, novaChave.tipoConta!!)

        log.info("Resultado da busca no ERP Itau: Status ${itauHttpResponse.status}. Body ${itauHttpResponse.body()}")

        when (itauHttpResponse.status) {
            NOT_FOUND -> throw IllegalStateException("Cliente não encontrado no Itau")
            OK -> {
            }
            else -> throw InternalServerError("Internal Server Error")
        }

        val itauReponseBody: ContaResponse = itauHttpResponse.body()!!
        val conta = erpItauService.toModel(itauReponseBody)
        val chave = novaChave.toModel(conta)
        log.info("Criando $chave")
        repository.save(chave)

        log.info("Solicitando cadastramento no BCB")
        val bcbRequest: BcbCreatePixKeyRequest =
            bcbService.createRequest(novaChave.tipoChave!!, conta, novaChave.chave)

        val bcbHttpResponse: HttpResponse<BcbPixKeyResponse> = bcbService.registra(bcbRequest)
        log.info("Resultado do registro no BCB: Status ${bcbHttpResponse.status}. Body ${bcbHttpResponse.body()}")

        if (bcbHttpResponse.status != CREATED)
            throw InternalServerError("Erro Inesperado")

        val bcbResponseBody: BcbPixKeyResponse = bcbHttpResponse.body()!!
        chave.informaCriacaoNoBcb(bcbResponseBody.key, bcbResponseBody.createdAt)
        return chave
    }


}