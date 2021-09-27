package dev.alexandrevieira.manager.endpoints.consulta

import dev.alexandrevieira.manager.apiclients.bcb.BcbService
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbPixKeyResponse
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.customexceptions.ChavePixNaoEncontradaException
import dev.alexandrevieira.manager.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {
    abstract fun filtra(repository: ChavePixRepository, bcbService: BcbService): ChavePixInfoResponse

    @Introspected
    data class PorPixId(
        @field:NotBlank @ValidUUID val clienteId: String,
        @field:NotBlank @ValidUUID val chavePixId: String
    ) : Filtro() {
        private val log = LoggerFactory.getLogger(this.javaClass)

        override fun filtra(repository: ChavePixRepository, bcbService: BcbService): ChavePixInfoResponse {
            log.info("Consultando repository. PixId: $chavePixId, ClienteId: $clienteId")
            return repository.findById(UUID.fromString(chavePixId))
                .filter { it.pertenceAoCliente(UUID.fromString(clienteId)) && it.criadaNoBcb }
                .map(ChavePixInfoResponse::of)
                .orElseThrow { ChavePixNaoEncontradaException("Chave não encontrada") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : Filtro() {
        private val log = LoggerFactory.getLogger(this.javaClass)
        override fun filtra(repository: ChavePixRepository, bcbService: BcbService): ChavePixInfoResponse {
            log.info("Consultando repository. Chave $chave")
            return repository.findByChave(chave)
                .map { ChavePixInfoResponse.of(it) }
                .orElseGet {
                    log.info("Chave não existe no repository. Consultando no BCB. Chave $chave")
                    val response: HttpResponse<BcbPixKeyResponse> = bcbService.consultaPorChave(chave)
                    log.info("Resultado da consulta no BCB: Status ${response.status}. Body ${response.body()}")
                    when (response.status) {
                        HttpStatus.OK -> ChavePixInfoResponse.of(response.body()!!)
                        else -> throw ChavePixNaoEncontradaException("Chave não encontrada")
                    }
                }
        }
    }

    @Introspected
    object Invalido : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbService: BcbService): ChavePixInfoResponse {
            throw IllegalArgumentException("Chave inválida ou não informada")
        }
    }
}
