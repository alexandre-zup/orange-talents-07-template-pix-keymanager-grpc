package dev.alexandrevieira.validation

import dev.alexandrevieira.endpoints.novachave.NovaChavePixValidated
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton

@Singleton
open class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, NovaChavePixValidated> {
//    private val cpfRegex = "^[0-9]{11}\$".toRegex()
//    private val celularRegex = "^\\+[1-9][0-9]\\d{1,14}\$".toRegex()
//    private val regexEmail = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex()


    override fun isValid(
        value: NovaChavePixValidated?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.tipoChave == null) return false

        return value.tipoChave.valida(value.chave)
    }

}