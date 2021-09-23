package dev.alexandrevieira.manager.data.repositories

import dev.alexandrevieira.manager.data.model.Titular
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface TitularRepository : JpaRepository<Titular, UUID>