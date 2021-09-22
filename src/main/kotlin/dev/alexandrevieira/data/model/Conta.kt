package dev.alexandrevieira.data.model

import dev.alexandrevieira.data.model.enums.TipoConta
import java.util.*
import javax.persistence.*

@Entity
class Conta(
    val agencia: String,
    val numero: String,
    @field:Enumerated(EnumType.STRING)
    val tipo: TipoConta,
    @field:ManyToOne(cascade = [CascadeType.PERSIST])
    val titular: Titular,
    @field:ManyToOne(cascade = [CascadeType.PERSIST])
    val instituicao: Instituicao
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun obterTitularId(): UUID {
        return titular.id
    }

    override fun toString(): String {
        return "ContaAssociada(agencia='$agencia', numero='$numero', tipo=$tipo, titular=$titular, instituicao=$instituicao, id=$id)"
    }
}