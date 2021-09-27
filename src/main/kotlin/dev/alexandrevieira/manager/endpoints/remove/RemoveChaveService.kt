package dev.alexandrevieira.manager.endpoints.remove

import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyResponse
import dev.alexandrevieira.manager.apiclients.bcb.BcbService
import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.customexceptions.ChavePixNaoEncontradaException
import dev.alexandrevieira.manager.exception.customexceptions.NaoAutorizadoException
import dev.alexandrevieira.manager.validation.ValidUUID
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import javax.transaction.Transactional

@Validated
@Singleton
class RemoveChaveService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var repository: ChavePixRepository

    @Inject
    private lateinit var bcbService: BcbService

    @Transactional
    fun remove(@ValidUUID chaveId: String, @ValidUUID clienteId: String) {
        log.info("chamado 'remove' com parâmetros: chaveId $chaveId, clienteId $clienteId")
        val optionalChave: Optional<ChavePix> = repository.findById(UUID.fromString(chaveId))
        if (optionalChave.isEmpty)
            throw ChavePixNaoEncontradaException("Chave $chaveId nao encontrada")

        val chave: ChavePix = optionalChave.get()
        if (!chave.pertenceAoCliente(UUID.fromString(clienteId)))
            throw NaoAutorizadoException("Nao autorizado")

        log.info("Removendo $chave")
        repository.delete(chave)

        log.info("Solicitando deleção no BCB")
        val bcbResponse: HttpResponse<BcbDeletePixKeyResponse> =
            bcbService.remove(chave.chave, chave.conta.instituicao.ispb)

        log.info("Solicitação de deleção no BCB. Status: ${bcbResponse.status}. Body: ${bcbResponse.body()}")

//          //a única hipótese da condição ser verdeira seria um 404, ou seja, a chave já não existe no BCB
//          //logo, não precisa fazer nada
//        if(bcbResponse.status != HttpStatus.OK)
//            throw IllegalStateException("Erro ao remover chave Pix no Banco Central")
    }
}