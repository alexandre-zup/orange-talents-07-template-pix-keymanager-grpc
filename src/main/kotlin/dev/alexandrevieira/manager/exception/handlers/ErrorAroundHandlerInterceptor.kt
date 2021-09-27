package dev.alexandrevieira.manager.exception.handlers

import dev.alexandrevieira.manager.exception.customexceptions.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.MessageSource
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.persistence.PersistenceException
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var messageSource: MessageSource

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        } catch (ex: Exception) {
            log.error("Handling ${ex.javaClass.simpleName} while processing the call: ${context.targetMethod}")
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status: Status = fromException(ex)
            responseObserver.onError(status.asRuntimeException())
        }

        return null
    }

    private fun fromException(ex: Exception): Status {
        return when (ex) {
            is NaoAutorizadoException -> status(Status.PERMISSION_DENIED, ex)
            is ServiceUnavailableException -> status(Status.UNAVAILABLE, ex)
            is IllegalStateException -> status(Status.FAILED_PRECONDITION, ex)
            is ConstraintViolationException -> status(Status.INVALID_ARGUMENT, ex)
            is PersistenceException -> status(Status.INVALID_ARGUMENT, ex)
            is ChavePixExistenteException -> status(Status.ALREADY_EXISTS, ex)
            is ChavePixNaoEncontradaException -> status(Status.NOT_FOUND, ex)
            is ChaveDeOutraInstituicaoException -> status(Status.PERMISSION_DENIED, ex)
            is InternalServerError -> status(Status.INTERNAL, ex)
            else -> Status.UNKNOWN.withCause(ex).withDescription("Ops, um erro inesperado ocorreu")
        }
    }

    private fun status(status: Status, ex: Exception): Status {
        return Status.fromCode(status.code).withCause(ex).withDescription(ex.message)
    }

}