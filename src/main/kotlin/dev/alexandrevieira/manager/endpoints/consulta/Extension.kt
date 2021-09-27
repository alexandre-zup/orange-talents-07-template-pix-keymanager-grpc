package dev.alexandrevieira.manager.endpoints.consulta

import dev.alexandrevieira.stubs.ConsultaChaveRequest
import dev.alexandrevieira.stubs.ConsultaChaveRequest.FiltroCase.*
import io.micronaut.validation.validator.Validator
import javax.validation.ConstraintViolationException

fun ConsultaChaveRequest.toModel(validator: Validator): Filtro {
    val filtro : Filtro = when (this.filtroCase) {
        PIXID -> Filtro.PorPixId(clienteId = pixId.clienteId, chavePixId = pixId.chavePixId)
        CHAVE -> Filtro.PorChave(chave)
        else -> Filtro.Invalido
    }

    val violations = validator.validate(filtro)
    if(violations.isNotEmpty())
        throw ConstraintViolationException(violations)

    return filtro
}