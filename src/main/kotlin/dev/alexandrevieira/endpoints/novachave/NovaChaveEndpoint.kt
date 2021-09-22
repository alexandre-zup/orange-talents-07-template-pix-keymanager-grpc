package dev.alexandrevieira.endpoints.novachave

import dev.alexandrevieira.*
import dev.alexandrevieira.data.model.ChavePix
import dev.alexandrevieira.data.repositories.ContaRepository
import dev.alexandrevieira.exception.handlers.ErrorHandler
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory


@ErrorHandler
@Singleton
class NovaChaveEndpoint : PixKeyManagerServiceGrpc.PixKeyManagerServiceImplBase() {
    @Inject
    lateinit var service: NovaChavePixService

    @Inject
    lateinit var client: ErpItauClient

    @Inject
    lateinit var repository: ContaRepository

    private val log = LoggerFactory.getLogger(NovaChaveEndpoint::class.java)

    override fun registra(request: NovaChavePixRequest?, observer: StreamObserver<NovaChavePixResponse>?) {
        log.info(request!!.toString())

        val requestComValidacoes : NovaChavePixValidated = request.comValidacoes()

        val chaveCriada: ChavePix = service.registra(requestComValidacoes)

        val response = NovaChavePixResponse.newBuilder()
            .setChavePixId(chaveCriada.id.toString())
            .setClienteId(chaveCriada.obterTitularId().toString())
            .build()

        observer?.onNext(response)
        observer?.onCompleted()
    }
}