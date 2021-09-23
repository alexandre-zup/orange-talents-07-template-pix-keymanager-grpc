package dev.alexandrevieira.manager.exception.handlers


import io.grpc.Status
import io.micronaut.context.MessageSource
import io.micronaut.context.MessageSource.MessageContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.hibernate.exception.ConstraintViolationException
import javax.persistence.PersistenceException

/**
 * The idea of this handler is to deal with database constraints errors, like unique or FK constraints for example
 */
@Singleton
class PersistenceExceptionHandler(@Inject var messageSource: MessageSource) :
    ExceptionHandler<PersistenceException> {

    override fun handle(e: PersistenceException): ExceptionHandler.StatusWithDetails {
        val cause = e.cause

        if (cause is ConstraintViolationException) {

            val constraintName = cause.constraintName
            if (constraintName.isNullOrBlank()) {
                return internalServerError(cause)
            }

            val message = messageSource.getMessage("data.integrity.error.$constraintName", MessageContext.DEFAULT)
            return message
                .map { alreadyExistsError(it, cause) } // TODO: dealing with many types of constraint errors
                .orElse(internalServerError(cause))
        } else {
            val status = when (cause) {
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(cause.message)
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(cause.message)
                else -> Status.UNKNOWN
            }
            return ExceptionHandler.StatusWithDetails(status.withCause(cause))
        }

    }

    override fun supports(e: Exception): Boolean {
        return e is PersistenceException
    }

    private fun alreadyExistsError(message: String?, e: ConstraintViolationException) =
        ExceptionHandler.StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(message)
                .withCause(e)
        )

    private fun internalServerError(e: ConstraintViolationException) =
        ExceptionHandler.StatusWithDetails(
            Status.INTERNAL
                .withDescription("Unexpected internal server error")
                .withCause(e)
        )
}