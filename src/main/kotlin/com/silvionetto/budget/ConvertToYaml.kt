package com.silvionetto.budget

import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.StringWriter
import java.util.*
import kotlin.collections.LinkedHashMap


fun main(args: Array<String>) {
    var inputFolder = ClassPathResource("input/store").file
    var stores = LinkedList<Store>()
    var json = LinkedHashMap<String, Object>()
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

    val yaml = Yaml()
    val writer = StringWriter()
    File("stores.json").writer().use { out ->
        stores.forEach {
            yaml.dump(it, writer)
            out.append(writer.toString())
        }
    }
}