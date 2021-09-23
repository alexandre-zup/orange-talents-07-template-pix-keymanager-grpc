package dev.alexandrevieira.manager.exception.alternative

import dev.alexandrevieira.manager.exception.customexceptions.ChavePixExistenteException
import dev.alexandrevieira.manager.exception.customexceptions.ChavePixNaoEncontradaException
import dev.alexandrevieira.manager.exception.customexceptions.InternalServerError
import dev.alexandrevieira.manager.exception.customexceptions.ServiceUnavailableException
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
            is ServiceUnavailableException -> status(Status.UNAVAILABLE, ex)
            is IllegalStateException -> status(Status.FAILED_PRECONDITION, ex)
            is ConstraintViolationException -> status(Status.INVALID_ARGUMENT, ex)
            is PersistenceException -> handlePersistenceException(ex)
            is ChavePixExistenteException -> status(Status.ALREADY_EXISTS, ex)
            is ChavePixNaoEncontradaException -> status(Status.NOT_FOUND, ex)
            is InternalServerError -> status(Status.INTERNAL, ex)
            else -> Status.UNKNOWN.withCause(ex).withDescription("Ops, um erro inesperado ocorreu")
        }
    }

    private fun status(status: Status, ex: Exception): Status {
        return Status.fromCode(status.code).withCause(ex).withDescription(ex.message)
    }

    private fun handlePersistenceException(e: PersistenceException): Status {
        val cause = e.cause

        if (cause is org.hibernate.exception.ConstraintViolationException) {
            val constraintName = cause.constraintName
            if (constraintName.isNullOrBlank()) {
                return Status.INVALID_ARGUMENT.withCause(cause).withDescription(cause.message)
            }

            val message = messageSource.getMessage(
                "data.integrity.error.$constraintName",
                MessageSource.MessageContext.DEFAULT
            )

            return Status.INVALID_ARGUMENT.withCause(cause).withDescription(message.orElse("dados invalidos"))
        } else {
            val message = cause?.message ?: e.message
            return Status.INVALID_ARGUMENT.withCause(cause ?: e).withDescription(message)
        }
    }
}