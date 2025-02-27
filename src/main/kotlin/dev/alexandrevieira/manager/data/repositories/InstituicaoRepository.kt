package dev.alexandrevieira.manager.data.repositories

import dev.alexandrevieira.manager.data.model.Instituicao
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface InstituicaoRepository : JpaRepository<Instituicao, Long> {
    fun findByIspb(ispb: String) : Optional<Instituicao>
}