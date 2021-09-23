package dev.alexandrevieira.manager.endpoints.novachave

import dev.alexandrevieira.stubs.NovaChavePixRequest
import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta

fun NovaChavePixRequest.comValidacoes() : NovaChavePixValidated {
    return NovaChavePixValidated(
        clienteId = clienteId,
        tipoChave = TipoChave.valueOf(tipoChave.name),
        chave = valorChave,
        tipoConta = TipoConta.valueOf(tipoConta.name)
    )
}