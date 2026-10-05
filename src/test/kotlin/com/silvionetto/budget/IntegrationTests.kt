package com.silvionetto.budget

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import java.time.Year
import org.hamcrest.Matchers.containsString

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class IntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Assert blog page returns ok`() {
        mockMvc.perform(get("/").with(user("admin").roles("USER")))
            .andExpect(status().isOk)
    }

    @Test
    fun `Home dashboard defaults to the current year and links to adjacent years`() {
        val currentYear = Year.now().value

        mockMvc.perform(get("/").with(user("admin").roles("USER")))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(">$currentYear<")))
            .andExpect(content().string(containsString("href=\"/year/${currentYear - 1}\"")))
            .andExpect(content().string(containsString("href=\"/year/${currentYear + 1}\"")))

        mockMvc.perform(get("/year/${currentYear - 1}").with(user("admin").roles("USER")))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(">${currentYear - 1}<")))

        mockMvc.perform(get("/year/${currentYear + 1}").with(user("admin").roles("USER")))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(">${currentYear + 1}<")))
    }

    @Test
    fun `Assert types page returns ok`() {
        mockMvc.perform(get("/types").with(user("admin").roles("USER")))
            .andExpect(status().isOk)
    }

    @Test
    fun `Unauthenticated requests are redirected to Google login`() {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/oauth2/authorization/google"))
    }
}
