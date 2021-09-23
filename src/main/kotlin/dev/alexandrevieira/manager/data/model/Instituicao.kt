package dev.alexandrevieira.manager.data.model

import javax.persistence.*

@Entity
class Instituicao(
    @field:Column(nullable = false)
    val nome: String,
    @field:Column(nullable = false, unique = true, updatable = false)
    val ispb: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}