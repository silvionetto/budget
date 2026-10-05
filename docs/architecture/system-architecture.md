# System architecture

## Architectural style
The application follows a classic Spring MVC + JPA server-rendered design.

- `@SpringBootApplication` boots the app
- `@Controller` endpoints serve HTML pages
- `@Service` classes encapsulate business logic
- `CrudRepository` interfaces provide persistence access
- Thymeleaf templates render server-side pages

## Main application flow
1. Startup runs the `databaseInitializer` bean.
2. It creates users and default category records.
3. The user interacts with UI pages served by `HtmlController`.
4. Services query JPA repositories.
5. Data is summarized into `Budget` objects.
6. Thymeleaf renders the page with the aggregated values.

## Key components

### Bootstrapping
File: `src/main/kotlin/com/silvionetto/budget/BudgetApplication.kt`

Responsibilities:
- app entry point
- disables banner output
- starts Spring Boot

### Seed data
File: `src/main/kotlin/com/silvionetto/budget/AppConfiguration.kt`

Responsibilities:
- creates admin users
- creates default categories and subcategories
- creates fallback `Unknown_Income` and `Unknown_Expense` categories

### Security
File: `src/main/kotlin/com/silvionetto/budget/SecurityConfig.kt`

Responsibilities:
- disables CSRF
- requires authentication for all requests
- configures HTTP Basic auth
- creates an in-memory `admin/admin` user

### Persistence
Files:
- `Entities.kt`
- `Repositories.kt`

Responsibilities:
- entity definitions
- JPA mappings
- repository query methods for category, store, and transaction lookup

### Business logic
File: `src/main/kotlin/com/silvionetto/budget/Services.kt`

Responsibilities:
- deduplicate stores and categories
- update store/category relationships
- compute income, expense, and yearly budget summaries
- group transactions by month

### HTTP interface
File: `src/main/kotlin/com/silvionetto/budget/HtmlController.kt`

Responsibilities:
- page routes
- model population
- year navigation helpers
- page-specific data loading for categories, stores, and transactions

## Template layer
`src/main/resources/templates`

Primary pages:
- `body.html` — dashboard
- `categories.html` — category list
- `category.html` — category detail
- `transactions.html` — transaction list
- `store.html` — edit store metadata

## Runtime configuration
`src/main/resources/application.properties`

Includes:
- Thymeleaf configuration
- JPA schema update setting
- Hibernate debug logging
- commented datasource credentials for external DB setup

## Related docs
- [Project overview](../overview/project-overview.md)
- [Domain model](../domain/domain-model.md)
- [Routes and controllers](../implementation/routes-and-controllers.md)
