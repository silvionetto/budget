# Reverse engineering of the budget application

## 1. Application purpose and architecture

This project is a Spring Boot/Kotlin personal budget dashboard. It is designed to track income and expenses by category, subcategory, month, and year, then render the results as HTML pages using Thymeleaf.

The application is organized around a classic server-rendered MVC structure:

- `BudgetApplication.kt` boots the Spring application.
- `AppConfiguration.kt` seeds the database with default categories and users at startup.
- `Entities.kt` defines the persistence model and domain objects.
- `Repositories.kt` exposes Spring Data JPA queries for lookup and aggregation.
- `Services.kt` contains the business logic for storing data and computing monthly budgets.
- `HtmlController.kt` handles browser routes and populates the model for the templates.
- `src/main/resources/templates` contains the user-facing pages.

The app uses:

- Spring Boot 3-style configuration with Kotlin
- Spring Data JPA for persistence
- Thymeleaf for server-side rendering
- Spring Security with HTTP Basic authentication
- an in-memory user with a fixed admin/admin credential

## 2. Runtime startup and bootstrap

### Application bootstrap

`BudgetApplication` is the entry point. It starts Spring Boot with banner disabled.

### Startup data seeding

`AppConfiguration` defines a bean named `databaseInitializer()` that runs at startup via `ApplicationRunner`.

It creates:

- two user records: `admin` and `silvionetto`
- several default categories and subcategories such as income, domestic expenses, health, education, transport, and savings
- special categories named `Unknown_Income` and `Unknown_Expense` used when a transaction store is created without an explicit category

This design makes the application self-populating in a demo or personal-use environment.

## 3. Domain model

The core domain classes live in `Entities.kt`.

### Base entity

`BaseEntity` is a JPA `@MappedSuperclass` and provides shared fields:

- `id: Long?`
- `version: Long?`
- `lastUpdateDate: Date`

Every persisted entity inherits from it, so all records carry a generated primary key, optimistic locking version, and a last-updated timestamp.

### Users

`User` is stored in the `users` table and includes:

- `login`
- `firstname`
- `lastname`
- `description` (optional)

### Category model

`BudgetCategory` maps to a category name and type:

- `name: String`
- `type: BudgetType` where `BudgetType` is `EXPENSE` or `INCOME`

It is constrained by uniqueness on the category name.

### Subcategory model

`BudgetSubCategory` belongs to a category and stores a `name` plus a `category` reference.

This creates a hierarchical structure:

- category: broad account bucket
- subcategory: finer-grained classification inside the category

### Store model

`Store` has:

- unique `name`
- `subCategory` relation to `BudgetSubCategory`

This means a store is not standalone; it is always associated with a subcategory, which in turn belongs to a category.

### Transaction model

`Transaction` is the financial event record. It includes:

- `date: Date`
- `store: Store`
- `account: String`
- `contraAccount: String`
- `code: String`
- `debitCredit: String`
- `amount: Double`
- `transactionType: String`
- `notifications: String` (`@Lob`)
- `subCategory: BudgetSubCategory`

It captures enough information to represent a bank statement line or imported financial record.

### Balance model

`Balance` contains:

- `date: Date`
- `openingBalance: Double`

It is more of a summary record than an active runtime object, but it represents a balance snapshot.

### Budget calculation helper

The non-persisted `Budget` class is used to represent a monthly budget summary for a category or overall totals.

Fields:

- `category: String`
- monthly values from January to December as `Double`

Methods:

- `getTotal()`: sum across all months
- `getAverage()`: arithmetic mean across the 12 months

This object is used heavily by the controller and template layer to display annual charts and totals.

## 4. Persistence layer and repositories

The repository layer is defined in `Repositories.kt`.

### UserRepository

- `findByLogin(login: String): User?`

### CategoryRepository

- `findByName(name: String): BudgetCategory?`
- `findByNameAndType(name: String, budgetType: BudgetType): BudgetCategory`
- `findByType(budgetType: BudgetType): List<BudgetCategory>`

### SubCategoryRepository

- `findByName(name: String): BudgetSubCategory?`
- `findByCategory(category: BudgetCategory): List<BudgetSubCategory>`
- `findByNameAndCategory(name: String, category: BudgetCategory): BudgetSubCategory`
- `findByCategoryType(type: BudgetType): List<BudgetSubCategory>`
- `findByNameAndCategoryType(name: String, type: BudgetType): List<BudgetSubCategory>`

### StoreRepository

- `findByName(name: String): Store?`
- `findBySubCategory(budgetSubCategory: BudgetSubCategory): List<Store>`

### TransactionRepository

The transaction repository includes filters for all common queries needed by the app:

- `findByDebitCredit(debitCredit: String)`
- `findByDate(date: Date)`
- `findBySubCategory(subCategory: BudgetSubCategory)`
- `findByStore(store: Store)`
- `findBySubCategoryCategory(category: BudgetCategory)`
- `findBySubCategoryCategoryAndDateGreaterThan(category: BudgetCategory, date: Date)`
- `findBySubCategoryCategoryAndDateBetween(category: BudgetCategory, startDate: Date, endDate: Date)`
- `findByDebitCreditAndDateBetween(debitCredit: String, startDate: Date, endDate: Date)`

This repository-focused structure makes the service layer straightforward and keeps the business logic compact.

## 5. Service layer and business rules

### UserService

`saveUser` prevents duplicate usernames by checking `findByLogin` before saving.

### StoreService

`saveStore(store: Store)` enforces uniqueness on the store name before saving.

`update(id, category, subCategory, name)`:

- loads the existing `Store`
- updates its name and assigned subcategory
- propagates the new subcategory to all transactions using that store
- saves the changed store

`saveStore(storeName: String, transactionSide: String)` creates a store under a default subcategory. If the transaction side is `Credit`, it uses the `Unknown_Income` category; otherwise it uses `Unknown_Expense`.

`saveStore(storeName: String, budgetType: String, subCategoryName: String)` creates a store using a category type and an explicit subcategory name.

### CategoryService

`saveCategory` persists a category only if it does not already exist.

### SubCategoryService

`saveSubCategory` persists a subcategory only if it does not already exist.

### TransactionService

`saveTransaction` avoids duplicates by checking if a transaction with the same `date`, `store.name`, and `amount` already exists.

`exists` is a basic deduplication rule rather than a full domain-specific uniqueness strategy.

The methods `getByCategoryAndMonth`, `getBySubCategoryAndMonth`, and `getBySubCategoryAndYearAndMonth` collect transactions for a specific time window, using `Calendar` and `Month` to split by month and year.

### BudgetService

This is the heart of the application.

`getBudgetByType()`:

- loads all transactions where `DebitCredit` is `Debit` or `Credit`
- groups them by month
- creates a `Budget` for `Income` and `Expense`
- also adds a `Total` row by subtracting expenses from income totals

`getBudget(year: String)`:

- iterates every category
- builds a monthly budget for that category for a specific year

`getBalance(year: String)`:

- gets income and expense totals for the year
- computes the monthly yield (income - expense)
- calculates a projected balance for each month with a running balance model

`getIncome(year)` and `getExpense(year)` fetch transactions between January 1 and January 1 of the following year, split into the month buckets, and return a yearly `Budget` object.

`getBudget(year, category)` does the same for a single category.

In summary, the service layer treats the budget as a set of monthly sums derived from transaction records, rather than as a fully normalized reporting model stored separately in the database.

## 6. Controller and HTTP flow

The main controller is `HtmlController` and is annotated with `@Controller` plus `@ConfigurationProperties("app")`.

### Application properties

The controller reads values from `app.properties` for:

- `title`
- `year`
- `previousYear`
- `nextYear`

### Main routes

| Route | Purpose |
| --- | --- |
| `/` | Home dashboard for the current year |
| `/year/{year}` | Dashboard for a selected year |
| `/types` | Type-based budget summary |
| `/categories` | List all categories |
| `/categories/{name}` | Category detail page |
| `/subcategories/{type}/{category}/{name}` | Subcategory detail page |
| `/transactions/{category}/{year}/{month}` | All transactions for a category and month |
| `/store/{id}` | Store edit page |
| `/balance/{year}` | Balance page |
| `/reports/{year}` | Reports page |

### Page behavior

- The home page loads `budgetService.getBudget(year)` and binds it to the `budgets` model attribute.
- The category page loads a category, all subcategories under it, all transactions for that category, and a per-category budget by month.
- The transactions page finds all subcategories for a category and aggregates transaction records by month and year.
- The store page loads the selected store, its category and subcategory, and all related transactions.

The controller also provides small helper methods for year navigation:

- `getPreviousYear(year)`
- `getNextYear(year)`

## 7. Templates and user interface

The app uses Thymeleaf templates and plain HTML with Bootstrap. The flow is server-rendered, not SPA-style.

### Shared layout

- `header.html` loads the Bootstrap CSS and sets the page title.
- `menu.html` renders the navigation bar with links to Home, Balance, Categories, Stores, and Reports.
- `footer.html` is included at the bottom of pages.

### Dashboard view (`body.html`)

The main dashboard shows a table with:

- year selector
- months (JAN to DEZ)
- total and average
- links to each category
- links to the monthly transaction detail pages for each category and month

The `Budget` objects are displayed as rows, where each row corresponds to either a category or an aggregate total.

### Categories view (`categories.html`)

Displays the list of categories as links to their detail pages.

### Category view (`category.html`)

Combines a summary table with the transaction list. The page shows the selected category and a list of monthly budget values for the category’s subcategories and totals.

### Transactions view (`transactions.html`)

Displays raw transaction rows with:

- date
- store name
- amount
- debit/credit type
- notes/notifications

### Store view (`store.html`)

Provides an editable form for changing:

- store name
- category
- subcategory

This page also displays the associated transaction history for the store.

## 8. Security model

`SecurityConfig.kt` configures Spring Security with:

- CSRF disabled
- all requests authenticated
- HTTP Basic authentication enabled

The in-memory user is:

- username: `admin`
- password: `admin`
- role: `USER`

This is intentionally simple and aimed at a local or personal budgeting tool rather than a production-grade multi-user SaaS app.

## 9. Configuration and environment

`application.properties` sets:

- Thymeleaf template resolution (`prefix=classpath:/templates/`, `suffix=.html`)
- `spring.jpa.hibernate.ddl-auto=update`
- debug logging for web requests and Hibernate SQL output

The datasource is deliberately commented out, which suggests that the app is expected to be configured with a local database connection in a deployment environment, while development may rely on an embedded or external relational database.

## 10. Notable implementation characteristics

A few patterns stand out from the code:

- The app is domain-driven but not fully normalized. It stores computed monthly budget values dynamically instead of persisting them as separate reporting tables.
- Several service methods intentionally create synthetic categories like `Unknown_Income` and `Unknown_Expense` to prevent data loss during import or manual store creation.
- The model uses a simple direct relationship: category → subcategory → store → transaction.
- The application is designed around a single user workflow rather than multi-tenant or multi-user authorization.
- The HTTP layer is lightweight and purely server-rendered; it does not expose a REST API.

## 11. Overall understanding

The application is best understood as a personal household finance tracker. It can ingest transaction-like records (such as bank statement lines), categorize them into budget groups, summarize them by month and year, and visualize the results in an HTML dashboard.

The real architectural center is the transaction-to-budget aggregation pipeline:

1. transaction records are created or imported,
2. each transaction belongs to a store and a subcategory,
3. the service layer groups those transactions by year/month and category,
4. the controller passes the aggregated `Budget` objects to Thymeleaf views,
5. the UI renders those totals and drill-down pages.

This makes the app a simple but effective accounting dashboard for a single owner.
