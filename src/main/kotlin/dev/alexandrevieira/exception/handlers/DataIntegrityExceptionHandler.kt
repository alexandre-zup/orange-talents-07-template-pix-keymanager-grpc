package dev.alexandrevieira.exception.handlers


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
class DataIntegrityExceptionHandler(@Inject var messageSource: MessageSource) :
    ExceptionHandler<PersistenceException> {

    override fun handle(persistenceException: PersistenceException): ExceptionHandler.StatusWithDetails {
        val e = persistenceException.cause

        if (e is ConstraintViolationException) {

            val constraintName = e.constraintName
            if (constraintName.isNullOrBlank()) {
                return internalServerError(e)
            }

            val message = messageSource.getMessage("data.integrity.error.$constraintName", MessageContext.DEFAULT)
            return message
                .map { alreadyExistsError(it, e) } // TODO: dealing with many types of constraint errors
                .orElse(internalServerError(e))
        } else {
            val status = when (e) {
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
                else -> Status.UNKNOWN
            }
            return ExceptionHandler.StatusWithDetails(status.withCause(e))
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