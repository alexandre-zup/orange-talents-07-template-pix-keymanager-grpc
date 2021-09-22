package dev.alexandrevieira

import dev.alexandrevieira.apiclients.erpitau.ContaResponse
import dev.alexandrevieira.data.model.enums.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "http://\${values.erp-itau.host}:\${values.erp-itau.port}")
interface ErpItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscaConta(@PathVariable clienteId: String, @QueryValue tipo: TipoConta): HttpResponse<ContaResponse>
}