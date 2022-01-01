package com.silvionetto.budget

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BudgetApplication

fun main(args: Array<String>) {
	runApplication<BudgetApplication>(*args) {
		setBannerMode(Banner.Mode.OFF)
	}
}
