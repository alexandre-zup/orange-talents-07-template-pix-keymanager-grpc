package dev.alexandrevieira.endpoints.novachave

import dev.alexandrevieira.validation.ValidPixKey
import dev.alexandrevieira.validation.ValidUUID
import dev.alexandrevieira.data.model.ChavePix
import dev.alexandrevieira.data.model.Conta
import dev.alexandrevieira.data.model.enums.TipoChave
import dev.alexandrevieira.data.model.enums.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidPixKey
data class NovaChavePixValidated(
    @field:ValidUUID
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {
    fun toModel(@Valid conta: Conta): ChavePix {
        return ChavePix(
            tipo = TipoChave.valueOf(tipoChave!!.name),
            chave = if (tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else chave!!,
            conta = conta
        )
    }
}