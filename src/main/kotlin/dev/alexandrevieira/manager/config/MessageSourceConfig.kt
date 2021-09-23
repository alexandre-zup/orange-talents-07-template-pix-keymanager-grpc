package dev.alexandrevieira.manager.config

import io.micronaut.context.MessageSource
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.i18n.ResourceBundleMessageSource
import io.micronaut.runtime.context.CompositeMessageSource
import jakarta.inject.Singleton

@Factory
class MessageSourceConfig {

    @Bean
    @Singleton
    fun messageSource(): MessageSource {
        return CompositeMessageSource(listOf(
            ResourceBundleMessageSource("messages") // messages.properties
        ))
    }
}