# Source map

This document maps the main source files to their roles in the application.

## Kotlin application code

### Boot and config
- `src/main/kotlin/com/silvionetto/budget/BudgetApplication.kt` — Spring Boot entry point
- `src/main/kotlin/com/silvionetto/budget/AppConfiguration.kt` — seed data and startup initialization
- `src/main/kotlin/com/silvionetto/budget/SecurityConfig.kt` — HTTP auth and security policy
- `src/main/resources/application.properties` — runtime config

### Domain and persistence
- `src/main/kotlin/com/silvionetto/budget/Entities.kt` — entities, enums, and `Budget` helper
- `src/main/kotlin/com/silvionetto/budget/Repositories.kt` — Spring Data JPA repository interfaces
- `src/main/kotlin/com/silvionetto/budget/Constants.kt` — financial labels and constants

### Business logic
- `src/main/kotlin/com/silvionetto/budget/Services.kt` — category, store, transaction, and budget logic

### HTTP layer
- `src/main/kotlin/com/silvionetto/budget/HtmlController.kt` — route handlers and model binding

## Templates
- `src/main/resources/templates/body.html` — dashboard
- `src/main/resources/templates/categories.html` — category index
- `src/main/resources/templates/category.html` — category details
- `src/main/resources/templates/transactions.html` — transaction table
- `src/main/resources/templates/store.html` — store edit form
- `src/main/resources/templates/menu.html` — nav bar
- `src/main/resources/templates/header.html` — shared page header
- `src/main/resources/templates/footer.html` — footer markup

## Tests
- `src/test/kotlin/com/silvionetto/budget/IntegrationTests.kt`
- `src/test/kotlin/com/silvionetto/budget/RepositoriesTests.kt`
- `src/test/kotlin/com/silvionetto/budget/SpringBootDemoApplicationTests.kt`

## Related docs
- [Project overview](../overview/project-overview.md)
- [Domain model](../domain/domain-model.md)
- [System architecture](../architecture/system-architecture.md)
