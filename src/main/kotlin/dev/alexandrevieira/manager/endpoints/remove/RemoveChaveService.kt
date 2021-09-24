package dev.alexandrevieira.manager.endpoints.remove

import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.customexceptions.ChavePixNaoEncontradaException
import dev.alexandrevieira.manager.exception.customexceptions.NaoAutorizadoException
import dev.alexandrevieira.manager.validation.ValidUUID
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*

@Validated
@Singleton
class RemoveChaveService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var repository: ChavePixRepository

    fun remove(@ValidUUID chaveId: String, @ValidUUID clienteId: String) {
        log.info("remove chamado para chaveId $chaveId e clienteId $clienteId")
        val optionalChave: Optional<ChavePix> = repository.findById(UUID.fromString(chaveId))
        if (optionalChave.isEmpty)
            throw ChavePixNaoEncontradaException("Chave $chaveId nao encontrada")

        val chave: ChavePix = optionalChave.get()
        if (!chave.pertenceAoCliente(UUID.fromString(clienteId)))
            throw NaoAutorizadoException("Nao autorizado")
        
        repository.delete(chave)
    }
}