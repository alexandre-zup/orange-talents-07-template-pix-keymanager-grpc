package dev.alexandrevieira.manager.endpoints.lista

import com.google.protobuf.Timestamp
import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.stubs.ListaChaveResponse
import dev.alexandrevieira.stubs.TipoDaChave
import dev.alexandrevieira.stubs.TipoDaConta
import java.time.ZoneOffset

fun List<ChavePix>.toResponse(): ListaChaveResponse {
    val responseBuilder = ListaChaveResponse.newBuilder()

    this.map {
        chaveInfoOf(it)
    }.run {
        return responseBuilder.addAllChaves(this).build()
    }
}

private fun chaveInfoOf(chavePix: ChavePix): ListaChaveResponse.ChaveInfo {
    val instant = chavePix.criadaEm.toInstant(ZoneOffset.UTC)
    val criadaEm = Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()

    return ListaChaveResponse.ChaveInfo.newBuilder()
        .setChavePixId(chavePix.id.toString())
        .setClienteId(chavePix.obterTitularId().toString())
        .setTipo(TipoDaChave.valueOf(chavePix.tipo.name))
        .setChave(chavePix.chave)
        .setTipoConta(TipoDaConta.valueOf(chavePix.conta.tipo.name))
        .setCriadaEm(criadaEm)
        .build()
}