package com.silvionetto.budget

import com.silvionetto.budget.xjb.BudgetCategory
import com.silvionetto.budget.xjb.BudgetSubCategory
import com.silvionetto.budget.xjb.BudgetType
import com.silvionetto.budget.xjb.Store
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.lang.Boolean
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import kotlin.Array
import kotlin.String


fun main(args: Array<String>) {
    var inputFolder = ClassPathResource("input/store").file
    var stores = LinkedList<com.silvionetto.budget.xjb.Store>()
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
                        var category = BudgetCategory()
                        category.name = categoryName
                        category.type = BudgetType.valueOf(categoryType)

                        if (subCategoryName != null) {
                            var subCategory = BudgetSubCategory()
                            subCategory.name = subCategoryName
                            subCategory.type = category
                            //var subCategory = BudgetSubCategory(subCategoryName, category)

                            if (storeName != null) {

                                var store = Store()
                                store.name = storeName
                                store.subCategory = subCategory
                                stores.add(store)
                            }
                        }

                    }
                }
            }
        }
    }

    val context = JAXBContext.newInstance(com.silvionetto.budget.xjb.Store::class.java)
    val mar: Marshaller = context.createMarshaller()
    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
    val runCount = AtomicInteger(0)
    stores.forEach {
        println(it.name)
        mar.marshal(it, File("target/stores/stores${runCount.addAndGet(1)}.xml"))
    }
}