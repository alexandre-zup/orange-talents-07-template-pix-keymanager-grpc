package dev.alexandrevieira.manager.apiclients.bcb

import dev.alexandrevieira.manager.apiclients.bcb.dto.*
import dev.alexandrevieira.manager.data.model.Conta
import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.exception.customexceptions.ChaveDeOutraInstituicaoException
import dev.alexandrevieira.manager.exception.customexceptions.InternalServerError
import dev.alexandrevieira.manager.exception.customexceptions.ServiceUnavailableException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class BcbService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var bcbClient: BcbClient

    fun createRequest(
        tipoDaChave: TipoChave,
        conta: Conta,
        valorChave: String
    ): BcbCreatePixKeyRequest {
        val keyType: KeyType = tipoDaChave.converte()
        val bankAccount = BankAccountDTO(conta)
        val owner = OwnerDTO(conta.titular)
        return BcbCreatePixKeyRequest(keyType, valorChave, bankAccount, owner)
    }

    fun registra(request: BcbCreatePixKeyRequest): HttpResponse<BcbCreatePixKeyResponse> {
        val bcbHttpResponse: HttpResponse<BcbCreatePixKeyResponse>
        try {
            bcbHttpResponse = bcbClient.registra(request)
        } catch (ex: HttpClientResponseException) {
            log.error("Erro no registro no BCB: Status ${ex.status}. Mensagem ${ex.message}")
            if (ex.status == HttpStatus.UNPROCESSABLE_ENTITY)
                throw IllegalStateException("Chave já cadastrada no BCB")
            else
                throw InternalServerError("Erro inesperado")
        } catch (ex: HttpClientException) {
            log.error("Erro na conexão com BCB: ${ex.message}")
            throw ServiceUnavailableException("Serviço indisponível")
        }
        return bcbHttpResponse
    }

    fun remove(key: String, participant: String): HttpResponse<BcbDeletePixKeyResponse> {
        val bcbRequest = BcbDeletePixKeyRequest(key, participant)
        val bcbResponse: HttpResponse<BcbDeletePixKeyResponse>

        try {
            bcbResponse = bcbClient.remove(bcbRequest.key, bcbRequest)
        } catch (e: HttpClientResponseException) {
            if (e.status == HttpStatus.FORBIDDEN)
                throw ChaveDeOutraInstituicaoException("Chave pertence a outra instituição")
            else
                throw InternalServerError("Erro inesperado")

        } catch (e: HttpClientException) {
            log.error("Erro de conexão com ERP: ${e.message}")
            throw ServiceUnavailableException("Serviço indisponível")
        }

        return bcbResponse
    }
}