package dev.alexandrevieira.manager.apiclients.bcb

import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbCreatePixKeyRequest
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbCreatePixKeyResponse
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyRequest
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbDeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://\${values.bcb.host}:\${values.bcb.port}/api/v1/pix/keys")
interface BcbClient {
    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun registra(@Body request: BcbCreatePixKeyRequest): HttpResponse<BcbCreatePixKeyResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun remove(@PathVariable key: String, @Body request: BcbDeletePixKeyRequest) : HttpResponse<BcbDeletePixKeyResponse>
}