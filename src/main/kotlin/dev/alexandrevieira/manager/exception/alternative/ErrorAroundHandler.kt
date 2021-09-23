package dev.alexandrevieira.manager.exception.alternative

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(CLASS, FIELD, TYPE)
@Around
annotation class ErrorAroundHandler