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
    lateinit var transactionService: TransactionService

    @Bean
    @Order(1)
    fun databaseInitializer() = ApplicationRunner {

        userService.saveUser(User("admin", "admin", "admin"))
        userService.saveUser(User("silvionetto", "Silvio", "Netto"))

        loadFiles()
    }

    fun loadFiles() {
        fun getLine(l : String): List<String> {
            if (l.startsWith("\"")) {
                return l.replace(", '",", \"").replace("',", "\",").replace("(","").replace(")", "").split("\", \"")
            }
            return l.replace("(","").replace(")", "").split(", '")
        }

        fun getStoreName(column: String): String {
            if (column.startsWith("\"")) {
                return column.replace("\"","")
            }
            return column.replace("'","")
        }

//        fun getColumns(l : String): Map<String, String> {
//            var line = getLine(l)
//            var columns = LinkedHashMap<String, String>()
//            //columns['storeName'] = getStoreName(l)
//            //columns['transactionSide'] =
//
//        }



        val storeResource = ClassPathResource("input/store")
        if (storeResource.exists()) {
            val inputFolder = storeResource.file
            inputFolder.walk().forEach {
                if (it.isFile) {
                    it.forEachLine { line ->
                        val columns = getLine(line)

                        val storeName = getStoreName(line)
                        val transactionSide = columns[1].replace("'","")
                        val category = columns[2].replace("'","")
                        val subCategoryName = columns[3].replace("'","")
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
                    var lineNumber: Int = "0".toInt()
                    it.forEachLine { line ->
                        if (lineNumber.compareTo(0) == 0) {
                            lineNumber++
                            return@forEachLine
                        }
                        var columns = line.split("\",\"")
                        if (columns.size == 1) {
                            columns = line.split("\";\"")
                        }

                        val date = columns[0].replace("\"", "")
                        val storeName = columns[1].replace("\"", "")
                        val account = columns[2].replace("\"", "")
                        val contraAccount = columns[3].replace("\"", "")
                        val code = columns[4].replace("\"", "")
                        val transactionSide = columns[5].replace("\"", "")
                        val amount = columns[6].replace("\"", "").replace(",",".")
                        val transactionType = columns[7].replace("\"", "")
                        val notifications = columns[8].replace("\"", "")
                        val store = storeService.saveStore(storeName, transactionSide)
                        transactionService.saveTransaction(Transaction(date.toDate(), store, account, contraAccount, code, transactionSide, amount.toDouble(), transactionType, notifications, store.subCategory))

                        lineNumber++
                    }
                }
            }
        }


    }
}