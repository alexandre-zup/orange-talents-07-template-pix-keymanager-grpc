package dev.alexandrevieira.manager.endpoints.remove

import dev.alexandrevieira.manager.exception.handlers.ErrorAroundHandler
import dev.alexandrevieira.stubs.KeyManagerRemoveServiceGrpc
import dev.alexandrevieira.stubs.RemoveChaveRequest
import dev.alexandrevieira.stubs.RemoveChaveResponse
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@ErrorAroundHandler
@Singleton
class RemoveChaveEndpoint : KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase() {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var service: RemoveChaveService

    override fun remove(request: RemoveChaveRequest?, responseObserver: StreamObserver<RemoveChaveResponse>?) {
        log.info(request!!.toString())
        service.remove(chaveId = request.chavePixId, clienteId = request.clienteId)
        responseObserver?.onNext(
            RemoveChaveResponse.newBuilder()
                .setChavePixId(request.chavePixId)
                .setClienteId(request.clienteId)
                .build()
        )
        responseObserver?.onCompleted()
    }
}