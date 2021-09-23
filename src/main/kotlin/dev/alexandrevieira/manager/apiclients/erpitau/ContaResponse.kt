package dev.alexandrevieira.manager.apiclients.erpitau

import io.micronaut.core.annotation.Introspected
import java.util.*

@Introspected
data class ContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {

    @Introspected
    data class InstituicaoResponse(
        val nome: String,
        val ispb: String
    )

    @Introspected
    data class TitularResponse(
        val id: UUID,
        val nome: String,
        val cpf: String
    )
}