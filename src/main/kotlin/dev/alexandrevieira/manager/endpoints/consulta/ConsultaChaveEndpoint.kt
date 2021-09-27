package dev.alexandrevieira.manager.endpoints.consulta

import dev.alexandrevieira.manager.apiclients.bcb.BcbService
import dev.alexandrevieira.manager.data.repositories.ChavePixRepository
import dev.alexandrevieira.manager.exception.handlers.ErrorAroundHandler
import dev.alexandrevieira.stubs.ConsultaChaveRequest
import dev.alexandrevieira.stubs.ConsultaChaveResponse
import dev.alexandrevieira.stubs.KeyManagerConsultaServiceGrpc
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
@ErrorAroundHandler
class ConsultaChaveEndpoint : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceImplBase() {
    val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var bcbService: BcbService

    @Inject
    private lateinit var repository: ChavePixRepository

    @Inject
    private lateinit var validator: Validator

    override fun consulta(request: ConsultaChaveRequest?, observer: StreamObserver<ConsultaChaveResponse>?) {
        log.info("chamado 'consulta' com request: ${request!!}")

        val filtro: Filtro = request.toModel(validator)
        val chaveInfo: ChavePixInfoResponse = filtro.filtra(repository, bcbService)

        observer?.onNext(chaveInfo.toConsultaChaveResponse(chaveInfo))
        observer?.onCompleted()
    }
}