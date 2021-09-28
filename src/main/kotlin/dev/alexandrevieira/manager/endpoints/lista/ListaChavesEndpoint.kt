package dev.alexandrevieira.manager.endpoints.lista

import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.handlers.ErrorAroundHandler
import dev.alexandrevieira.stubs.KeyManagerListaServiceGrpc
import dev.alexandrevieira.stubs.ListaChaveRequest
import dev.alexandrevieira.stubs.ListaChaveResponse
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*

@ErrorAroundHandler
@Singleton
class ListaChavesEndpoint : KeyManagerListaServiceGrpc.KeyManagerListaServiceImplBase() {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val uuidRegex: Regex = "[a-f0-9]{8}-[a-f0-9]{4}-[1-5][a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}".toRegex()

    @Inject
    private lateinit var repository: ChavePixRepository

    override fun lista(request: ListaChaveRequest, responseObserver: StreamObserver<ListaChaveResponse>) {
        log.info("chamado 'lista' com parâmetro: clienteId ${request.clienteId}")
        val clienteId = request.clienteId

        if (clienteId.isNullOrBlank() || !clienteId.matches(uuidRegex))
            throw IllegalArgumentException("Cliente Id deve ser válido")

        val lista: List<ChavePix> = repository.findAllByContaTitularId(UUID.fromString(clienteId))

        responseObserver.onNext(lista.toResponse())
        responseObserver.onCompleted()
    }
}