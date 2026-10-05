package com.silvionetto.budget

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource

@Configuration
class AppConfiguration {

    @Autowired
    lateinit var userService: UserService
    
    @Autowired
    lateinit var bankMovementImportService: BankMovementImportService

    @Bean
    @Order(1)
    fun databaseInitializer() = ApplicationRunner {

        userService.saveUser(User("admin", "admin", "admin"))
        userService.saveUser(User("silvionetto", "Silvio", "Netto"))

        loadFiles()
    }

    fun loadFiles() {
        val extractResource = ClassPathResource("input/extract")
        if (extractResource.exists()) {
            val inputFolder = extractResource.file
            inputFolder.walk().forEach {
                if (it.isFile) {
                    bankMovementImportService.importCsv(it.readBytes())
                }
            }
        }


    }
}