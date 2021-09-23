package dev.alexandrevieira.manager.data.model

import dev.alexandrevieira.manager.data.model.enums.TipoChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(uniqueConstraints = [UniqueConstraint(
    name = "uk_chavepix_chave",   // you must define the constraint name properly
    columnNames = ["chave"]
)])
class ChavePix(
    @field:NotNull
    @field:Valid
    @field:JoinColumn(nullable = false)
    @field:ManyToOne(cascade = [CascadeType.PERSIST])
    val conta: Conta,

    @field:NotBlank
    @field:Column(nullable = false, updatable = false)
    val chave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipo: TipoChave
) {
    @Id
    val id: UUID? = if(tipo == TipoChave.ALEATORIA) UUID.fromString(chave) else UUID.randomUUID()

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun obterTitularId(): UUID {
        return conta.obterTitularId()
    }
}