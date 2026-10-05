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
    lateinit var storeService: StoreService

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
        fun getLine(l: String): List<String> {
            if (l.startsWith("\"")) {
                return l.replace(", '", ", \"").replace("',", "\",").replace("(", "").replace(")", "").split("\", \"")
            }
            return l.replace("(", "").replace(")", "").split(", '")
        }

        fun getStoreName(column: String): String {
            if (column.startsWith("\"")) {
                return column.replace("\"", "")
            }
            return column.replace("'", "")
        }

        val storeResource = ClassPathResource("input/store")
        if (storeResource.exists()) {
            val inputFolder = storeResource.file
            inputFolder.walk().forEach {
                if (it.isFile) {
                    it.forEachLine { line ->
                        val columns = getLine(line)
                        val storeName = getStoreName(line)
                        val transactionSide = columns[1].replace("'", "")
                        val subCategoryName = columns[3].replace("'", "")
                        storeService.saveStore(storeName, transactionSide, subCategoryName)
                    }
                }
            }
        }

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