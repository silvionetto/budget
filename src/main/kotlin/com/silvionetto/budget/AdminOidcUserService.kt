package com.silvionetto.budget

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class AdminOidcUserService(
    private val adminEmail: String,
    private val delegate: OAuth2UserService<OidcUserRequest, OidcUser> = OidcUserService()
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val user = delegate.loadUser(userRequest)
        val email = user.getAttribute<String>("email")
        val emailVerified = user.getAttribute<Boolean>("email_verified")
        if (!isAllowedAdminEmail(email, emailVerified, adminEmail)) {
            throw OAuth2AuthenticationException(
                OAuth2Error("access_denied"),
                "The Google account is not authorized to access this application."
            )
        }
        return user
    }
}

internal fun isAllowedAdminEmail(email: String?, emailVerified: Boolean?, adminEmail: String): Boolean =
    emailVerified == true && !email.isNullOrBlank() && email.equals(adminEmail, ignoreCase = true)
