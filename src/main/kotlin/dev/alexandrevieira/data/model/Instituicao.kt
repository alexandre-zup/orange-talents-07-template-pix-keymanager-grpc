package dev.alexandrevieira.data.model

import javax.persistence.*

@Entity
class Instituicao(
    val nome: String,
    @field:Column(nullable = false, unique = true, updatable = false)
    val ispb: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}