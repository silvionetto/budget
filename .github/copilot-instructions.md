# Copilot Instructions for Budget

## Build, test, and validation

- Run the app: `./mvnw spring-boot:run` (use `mvnw.cmd` on Windows)
- Run the test suite: `./mvnw test`
- Run the full Maven verification/build pipeline: `./mvnw verify`
- Run a single test class: `./mvnw test -Dtest=IntegrationTests` or `./mvnw test -Dtest=RepositoriesTests`
- There is no separate lint task in this repository; Maven lifecycle commands are the project’s standard validation path.
- CI in `.github/workflows/budget-actions.yml` runs: `mvn --batch-mode --update-snapshots verify`

## High-level architecture

- This is a Spring Boot + Kotlin application for tracking a monthly budget and year-over-year reporting.
- `BudgetApplication.kt` bootstraps the app and starts the Spring context.
- `HtmlController.kt` is the central routing layer: it renders the server-side Thymeleaf pages for the home screen, category views, store views, balance, and reports.
- The persistence model is a hierarchical JPA tree: `BudgetCategory` -> `BudgetSubCategory` -> `Store` -> `Transaction`, with `Balance` used for overall account balances and a `Budget` view model used for monthly totals.
- `Repositories.kt` contains the Spring Data repository interfaces; default query patterns use method names such as `findByName`, `findBySubCategory`, and `findByDate` instead of custom SQL.
- `Services.kt` contains the business logic for seed data, duplicate prevention, store/category updates, and monthly budget calculations.
- `AppConfiguration.kt` runs on startup, populates default users and categories, and optionally loads CSV inputs from the resources directory.
- `SecurityConfig.kt` configures basic authentication with an in-memory `admin` account.
- View templates live under `src/main/resources/templates` and are tightly coupled to the model keys created by `HtmlController` (`title`, `year`, `budgets`, `category`, `subcategory`, etc.).

## Key conventions

- The app is server-rendered with Thymeleaf; it is not organized as a separate REST API or frontend client.
- Domain language is organized around budget entities rather than generic CRUD names: category, subcategory, store, and transaction are the primary concepts.
- The startup seeding code intentionally creates a default category/subcategory graph and is treated as part of the product’s baseline data. Changes there affect app behavior at boot time.
- Repository methods are intentionally named by Spring Data conventions; prefer matching that pattern instead of adding custom query code unless the existing approach is insufficient.
- Date handling is a recurring pattern in the service layer; budget calculations depend on `Date` and month-based filtering (`it.date.month + 1` or `SimpleDateFormat` for year/month ranges). Keep that logic consistent when editing transaction logic.
- Controller and template work is strongly coupled: route parameters and model keys must stay aligned with existing Thymeleaf views, especially for the budget and category screens.
- The project uses Maven as the build system and Kotlin/JPA conventions from Spring Boot 2.x; keep changes compatible with the current project setup rather than adopting newer framework assumptions unless they are explicitly being upgraded.
