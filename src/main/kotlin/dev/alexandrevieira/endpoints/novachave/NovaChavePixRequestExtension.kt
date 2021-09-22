package dev.alexandrevieira.endpoints.novachave

import dev.alexandrevieira.NovaChavePixRequest
import dev.alexandrevieira.data.model.enums.TipoChave
import dev.alexandrevieira.data.model.enums.TipoConta

fun NovaChavePixRequest.comValidacoes() : NovaChavePixValidated {
    return NovaChavePixValidated(
        clienteId = clienteId,
        tipoChave = TipoChave.valueOf(tipoChave.name),
        chave = valorChave,
        tipoConta = TipoConta.valueOf(tipoConta.name)
    )
}