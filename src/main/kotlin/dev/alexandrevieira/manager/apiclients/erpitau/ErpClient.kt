package dev.alexandrevieira.manager.apiclients.erpitau

import dev.alexandrevieira.manager.data.model.enums.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "http://\${values.erp-itau.host}:\${values.erp-itau.port}")
interface ErpClient {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun buscaConta(@PathVariable clienteId: String, @QueryValue tipo: TipoConta): HttpResponse<ContaResponse>
}