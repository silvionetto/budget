package com.silvionetto.budget

import org.springframework.core.io.ClassPathResource
import java.awt.print.Book
import java.io.File
import java.lang.Boolean
import java.util.*
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import kotlin.Array
import kotlin.String


fun main(args: Array<String>) {
    var inputFolder = ClassPathResource("input/store").file
    var stores = LinkedList<Store>()
    if (inputFolder.exists()) {
        inputFolder.walk().forEach {
            if (it.isFile) {
                it.forEachLine { line ->
                    var regex = """Store\(name=(.+),\ssubCategory""".toRegex()
                    var match = regex.find(line)
                    var storeName = match!!.groups[1]?.value

                    regex = """BudgetSubCategory\(name=(.+),\scategory""".toRegex()
                    match = regex.find(line)
                    var subCategoryName = match!!.groups[1]?.value

                    regex = """BudgetCategory\(name=(.+),\stype""".toRegex()
                    match = regex.find(line)
                    var categoryName = match!!.groups[1]?.value

                    regex = """type=(\w+)""".toRegex()
                    match = regex.find(line)
                    var categoryType = match!!.groups[1]?.value

                    if (categoryName != null && categoryType != null) {
                        var category = BudgetCategory(categoryName, BudgetType.valueOf(categoryType))

                        if (subCategoryName != null) {
                            var subCategory = BudgetSubCategory(subCategoryName, category)

                            if (storeName != null) {
                                var store = Store(storeName, subCategory)
                                stores.add(store)
                            }
                        }

                    }
                }
            }
        }
    }

    val context = JAXBContext.newInstance(Book::class.java)
    val mar: Marshaller = context.createMarshaller()
    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
    mar.marshal(stores, File("stores.xml"))
}