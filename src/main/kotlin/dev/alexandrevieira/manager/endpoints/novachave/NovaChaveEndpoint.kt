package dev.alexandrevieira.manager.endpoints.novachave

import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.exception.handlers.ErrorAroundHandler
import dev.alexandrevieira.stubs.KeyManagerRegistraServiceGrpc
import dev.alexandrevieira.stubs.NovaChavePixRequest
import dev.alexandrevieira.stubs.NovaChavePixResponse
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory


@ErrorAroundHandler
@Singleton
class NovaChaveEndpoint : KeyManagerRegistraServiceGrpc.KeyManagerRegistraServiceImplBase() {
    @Inject
    private lateinit var service: NovaChavePixService
    private val log = LoggerFactory.getLogger(NovaChaveEndpoint::class.java)

    override fun registra(request: NovaChavePixRequest?, observer: StreamObserver<NovaChavePixResponse>?) {
        log.info(request!!.toString())
        val requestComValidacoes: NovaChavePixValidated = request.comValidacoes()
        val chaveCriada: ChavePix = service.registra(requestComValidacoes)
        val response = NovaChavePixResponse.newBuilder()
            .setChavePixId(chaveCriada.id.toString())
            .setClienteId(chaveCriada.obterTitularId().toString())
            .build()

        observer?.onNext(response)
        observer?.onCompleted()
    }
}