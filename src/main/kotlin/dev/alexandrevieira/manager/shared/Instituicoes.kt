package dev.alexandrevieira.manager.shared

import io.micronaut.core.io.ResourceResolver
import io.micronaut.core.io.scan.ClassPathResourceLoader
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.charset.Charset

object Instituicoes {
    private val instituicoes: MutableMap<String, String>

    init {
        val log = LoggerFactory.getLogger(this::class.java)
        val loader: ClassPathResourceLoader =
            ResourceResolver().getLoader(ClassPathResourceLoader::class.java).get()
        val resource: URL = loader.getResource("classpath:ParticipantesSTR.csv").get()

        instituicoes = mutableMapOf()
        log.info("Carregando o arquivo de instituições")

        var count = 0
        File(resource.toURI()).readLines(Charsets.UTF_8).let { lines ->
            lines.forEach { line ->
                val split = line.split(",")
                if (split.size >= 2 && count > 0)
                    instituicoes[split[0].trim()] = split[1].trim()
                count++
            }
        }

        log.info("Instituições carregadas")
    }

    fun nome(ispb: String): String {
        return instituicoes[ispb] ?: throw IllegalStateException("Instituição não cadastrada")
    }
}
