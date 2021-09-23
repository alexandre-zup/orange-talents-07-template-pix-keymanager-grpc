package dev.alexandrevieira.manager.data.model

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Titular(
    @field:Id
    @field:Column(nullable = false, unique = true)
    val id: UUID,
    val nome: String,
    @field:Column(nullable = false, unique = true, updatable = false)
    val cpf: String
)