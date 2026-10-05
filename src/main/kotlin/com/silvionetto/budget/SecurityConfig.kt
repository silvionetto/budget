package com.silvionetto.budget

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(
        http: HttpSecurity,
        adminOidcUserService: OAuth2UserService<OidcUserRequest, OidcUser>
    ): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth -> auth.anyRequest().authenticated() }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.oidcUserService(adminOidcUserService)
                }
            }
        return http.build()
    }

    @Bean
    fun adminOidcUserService(
        @Value("\${app.admin-email}") adminEmail: String
    ): OAuth2UserService<OidcUserRequest, OidcUser> {
        return AdminOidcUserService(adminEmail)
    }
}