package dev.alexandrevieira.manager

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("dev.alexandrevieira")
		.start()
}

