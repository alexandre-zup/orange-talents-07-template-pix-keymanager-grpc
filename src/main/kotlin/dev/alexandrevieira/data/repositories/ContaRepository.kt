package dev.alexandrevieira.data.repositories

import dev.alexandrevieira.data.model.Conta
import dev.alexandrevieira.data.model.enums.TipoConta
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ContaRepository : JpaRepository<Conta, Long> {
    fun findByTitularIdAndTipo(titularId: UUID, tipo: TipoConta): Optional<Conta>
}