package com.silvionetto.budget

import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml

fun main(args: Array<String>) {
    var yaml = Yaml()
    if (inputStream.exists() && inputStream.isFile) {
        var result = yaml.load<Store>(inputStream.inputStream())
        println(result)
    }
}