package dev.alexandrevieira.manager.apiclients.bcb

import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbCreatePixKeyRequest
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyRequest
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyResponse
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbPixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://\${values.bcb.host}:\${values.bcb.port}/api/v1/pix/keys")
interface BcbClient {
    @Post
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun registra(@Body request: BcbCreatePixKeyRequest): HttpResponse<BcbPixKeyResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun remove(@PathVariable key: String, @Body request: BcbDeletePixKeyRequest): HttpResponse<BcbDeletePixKeyResponse>

    @Get("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun consulta(@PathVariable key: String): HttpResponse<BcbPixKeyResponse>
}