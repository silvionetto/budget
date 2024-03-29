package com.silvionetto.budget

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTests(@Autowired val restTemplate : TestRestTemplate) {

    @Test
    fun `Assert blog page title, content and status code`() {
        val entity = restTemplate.getForEntity<String>("/")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.FOUND)
    }

    @Test
    fun `Assert category name, content and status code`() {
        println(">> Assert category name, content and status code")
        val name = "Renda"
        val entity = restTemplate.getForEntity<String>("/category/${name}")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.FOUND)
    }
}