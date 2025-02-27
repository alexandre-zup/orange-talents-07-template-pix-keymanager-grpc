package dev.alexandrevieira.manager.data.repositories

import dev.alexandrevieira.manager.data.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String): Boolean
    fun findByChave(chave: String): Optional<ChavePix>
    fun findAllByContaTitularId(clienteId: UUID): List<ChavePix>
}