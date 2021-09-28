package dev.alexandrevieira.manager.endpoints.consulta

import dev.alexandrevieira.stubs.ConsultaChaveRequest
import dev.alexandrevieira.stubs.ConsultaChaveRequest.FiltroCase.CHAVE
import dev.alexandrevieira.stubs.ConsultaChaveRequest.FiltroCase.PIXID

fun ConsultaChaveRequest.paraFiltro(): Filtro {
    val filtro: Filtro = when (this.filtroCase) {
        PIXID -> Filtro.PorPixId(clienteId = pixId.clienteId, chavePixId = pixId.chavePixId)
        CHAVE -> Filtro.PorChave(chave)
        else -> Filtro.Invalido
    }

    return filtro
}