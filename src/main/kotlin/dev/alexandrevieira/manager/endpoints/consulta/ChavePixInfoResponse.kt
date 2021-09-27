package dev.alexandrevieira.manager.endpoints.consulta

import com.google.protobuf.Timestamp
import dev.alexandrevieira.manager.apiclients.bcb.dto.BcbPixKeyResponse
import dev.alexandrevieira.manager.data.model.ChavePix
import dev.alexandrevieira.manager.data.model.enums.TipoChave
import dev.alexandrevieira.manager.data.model.enums.TipoConta
import dev.alexandrevieira.manager.shared.Instituicoes
import dev.alexandrevieira.stubs.ConsultaChaveResponse
import dev.alexandrevieira.stubs.TipoDaChave
import dev.alexandrevieira.stubs.TipoDaConta
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Introspected
data class ChavePixInfoResponse(
    val chavePixId: String,
    val tipo: TipoChave,
    val chave: String,
    val criadaEm: LocalDateTime,
    val conta: ContaInfoResponse
) {
    fun toConsultaChaveResponse(info: ChavePixInfoResponse): ConsultaChaveResponse {
        val titularBuilder = ConsultaChaveResponse.ContaInfo.TitularInfo.newBuilder()
            .setId(info.conta.titular.id.toString())
            .setCpf(info.conta.titular.cpf)
            .setNome(info.conta.titular.nome)

        val instituicaoBuilder = ConsultaChaveResponse.ContaInfo.InstituicaoInfo.newBuilder()
            .setIspb(info.conta.instituicao.ispb)
            .setNome(info.conta.instituicao.nome)


        val contaBuilder = ConsultaChaveResponse.ContaInfo.newBuilder()
            .setAgencia(info.conta.agencia)
            .setNumero(info.conta.numero)
            .setTipo(TipoDaConta.valueOf(info.conta.tipo.name))
            .setInstituicao(instituicaoBuilder)
            .setTitular(titularBuilder)


        val instant = info.criadaEm.toInstant(ZoneOffset.UTC)
        val timestamp = Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()
        return ConsultaChaveResponse.newBuilder()
            .setChavePixId(info.chavePixId)
            .setTipo(TipoDaChave.valueOf(info.tipo.name))
            .setChave(info.chave)
            .setCriadaEm(timestamp)
            .setConta(contaBuilder)
            .build()
    }

    companion object {
        fun of(chave: ChavePix): ChavePixInfoResponse {
            val iAux = chave.conta.instituicao
            val instituicao = ContaInfoResponse.InstituicaoInfoResponse(ispb = iAux.ispb, nome = iAux.nome)

            val tAux = chave.conta.titular
            val titular = ContaInfoResponse.TitularInfoResponse(id = tAux.id.toString(), tAux.nome, tAux.cpf)

            val cAux = chave.conta
            val conta = ContaInfoResponse(cAux.agencia, cAux.numero, cAux.tipo, instituicao, titular)

            return ChavePixInfoResponse(chave.id.toString(), chave.tipo, chave.chave, chave.criadaEm, conta)
        }

        fun of(key: BcbPixKeyResponse): ChavePixInfoResponse {
            val instituicao = ContaInfoResponse.InstituicaoInfoResponse(
                nome = Instituicoes.nome(key.bankAccount.participant),
                ispb = key.bankAccount.participant
            )

            val owner = key.owner
            val titular = ContaInfoResponse.TitularInfoResponse(
                id = "",
                nome = owner.name,
                cpf = owner.taxIdNumber
            )

            val cAux = key.bankAccount
            val conta = ContaInfoResponse(
                agencia = cAux.branch,
                numero = cAux.accountNumber,
                tipo = cAux.accountType.convert(),
                instituicao = instituicao,
                titular = titular
            )
            return ChavePixInfoResponse(
                chavePixId = "",
                tipo = key.keyType.convert(),
                chave = key.key,
                criadaEm = key.createdAt,
                conta = conta
            )
        }
    }

    @Introspected
    data class ContaInfoResponse(
        val agencia: String,
        val numero: String,
        val tipo: TipoConta,
        val instituicao: InstituicaoInfoResponse,
        val titular: TitularInfoResponse
    ) {

        @Introspected
        data class InstituicaoInfoResponse(
            val ispb: String,
            val nome: String
        )

        @Introspected
        data class TitularInfoResponse(
            val id: String,
            val nome: String,
            val cpf: String
        )
    }
}