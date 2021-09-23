package dev.alexandrevieira.apiclients.erpitau

import dev.alexandrevieira.data.model.Conta
import dev.alexandrevieira.data.model.Instituicao
import dev.alexandrevieira.data.model.Titular
import dev.alexandrevieira.data.model.enums.TipoConta
import dev.alexandrevieira.data.repositories.ChavePixRepository
import dev.alexandrevieira.data.repositories.ContaRepository
import dev.alexandrevieira.data.repositories.InstituicaoRepository
import dev.alexandrevieira.data.repositories.TitularRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ConverterService {
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