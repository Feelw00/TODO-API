package com.lucas.aladin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AladinApplication

fun main(args: Array<String>) {
	runApplication<AladinApplication>(*args)
}
