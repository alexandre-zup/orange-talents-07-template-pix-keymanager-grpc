package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.data.model.Titular
import io.micronaut.core.annotation.Introspected

@Introspected
data class OwnerDTO(
    val type: PersonType,
    val name: String,
    val taxIdNumber: String
) {
    constructor(titular: Titular) : this(PersonType.NATURAL_PERSON, titular.nome, titular.cpf)
}