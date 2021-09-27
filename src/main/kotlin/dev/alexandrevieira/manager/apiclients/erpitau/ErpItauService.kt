package dev.alexandrevieira.manager.apiclients.erpitau

import dev.alexandrevieira.manager.data.model.Conta
import dev.alexandrevieira.manager.data.model.Instituicao
import dev.alexandrevieira.manager.data.model.Titular
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.data.repositories.ContaRepository
import dev.alexandrevieira.manager.data.repositories.InstituicaoRepository
import dev.alexandrevieira.manager.data.repositories.TitularRepository
import dev.alexandrevieira.manager.exception.customexceptions.ServiceUnavailableException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class ErpItauService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Inject
    private lateinit var itauClient: ErpClient

    fun buscaConta(clienteId: String, tipoConta: TipoConta): HttpResponse<ContaResponse> {
        val itauHttpResponse: HttpResponse<ContaResponse>

        try {
            itauHttpResponse = itauClient.buscaConta(clienteId, tipoConta)
        } catch (e: HttpClientException) {
            log.error("Erro de conexão com ERP: ${e.message}")
            throw ServiceUnavailableException("Serviço indisponível")
        }
        return itauHttpResponse
    }

    @Inject
    lateinit var titularRepository: TitularRepository

    @Inject
    lateinit var instituicaoRepository: InstituicaoRepository

    @Inject
    lateinit var contaRepository: ContaRepository


    fun toModel(dados: ContaResponse): Conta {
        val optionalConta = contaRepository.findByTitularIdAndTipo(
            titularId = dados.titular.id,
            tipo = TipoConta.valueOf(dados.tipo)
        )

        return if (optionalConta.isPresent) return optionalConta.get()
        else Conta(
            agencia = dados.agencia,
            numero = dados.numero,
            tipo = TipoConta.valueOf(dados.tipo),
            titular = toModel(dados.titular),
            instituicao = toModel(dados.instituicao)
        )

    }

    fun toModel(dados: ContaResponse.InstituicaoResponse): Instituicao {
        val optionalInstituicao = instituicaoRepository.findByIspb(dados.ispb)

        return if (optionalInstituicao.isPresent) optionalInstituicao.get()
        else Instituicao(dados.nome, dados.ispb)
    }

    fun toModel(dados: ContaResponse.TitularResponse): Titular {
        val optionalTitular = titularRepository.findById(dados.id)

        return if(optionalTitular.isPresent) optionalTitular.get()
        else Titular(dados.id, dados.nome, dados.cpf)
    }
}