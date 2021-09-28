package dev.alexandrevieira.manager.shared

object Instituicoes {
    private val instituicoes: Map<String, String> = mapOf("60701190" to "ITAÚ UNIBANCO S.A.")

    fun nome(ispb: String): String {
        return instituicoes[ispb] ?: throw IllegalStateException("Instituição não cadastrada")
    }
}