package dev.alexandrevieira.manager

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
    build().args(*args).packages("dev.alexandrevieira").start()
}

