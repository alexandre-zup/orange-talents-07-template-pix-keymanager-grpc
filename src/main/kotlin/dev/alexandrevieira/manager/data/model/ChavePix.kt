package dev.alexandrevieira.manager.data.model

import dev.alexandrevieira.manager.data.model.enums.TipoChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "uk_chavepix_chave",   // you must define the constraint name properly
        columnNames = ["chave"]
    )]
)
class ChavePix(
    @field:NotNull
    @field:Valid
    @field:JoinColumn(nullable = false)
    @field:ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val conta: Conta,

    chave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipo: TipoChave
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    @field:NotNull
    @field:Column(nullable = false, length = 77)
    var chave: String = chave
        private set

    @field:NotNull
    @field:Column(nullable = false)
    var criadaEm: LocalDateTime = LocalDateTime.now()
        private set

    @field:NotNull
    @field:Column(nullable = false)
    var criadaNoBcb: Boolean = false
        private set

    fun obterTitularId(): UUID {
        return conta.obterTitularId()
    }

    fun pertenceAoCliente(clienteId: UUID): Boolean {
        return conta.obterTitularId() == clienteId
    }

    fun informaCriacaoNoBcb(novaChave: String, criadaEmBcb: LocalDateTime) {
        if (tipo == TipoChave.ALEATORIA)
            this.chave = novaChave

        this.criadaEm = criadaEmBcb
        this.criadaNoBcb = true
    }

    override fun toString(): String {
        return "ChavePix(tipo=$tipo, chave=$chave, criadaEm=$criadaEm, instituicao=${conta.instituicao.ispb}, titular=${conta.titular.cpf})"
    }
}