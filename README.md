# Budget

A Spring Boot/Kotlin monthly budget application for tracking income, expenses, categories, subcategories, stores, and transactions.

## Documentation

- [Reverse engineering guide](docs/reverse-engineering.md) — a domain and architecture summary derived from the codebase.

## PostgreSQL database

The application uses PostgreSQL by default. Start the database from the repository root:

```powershell
docker compose up -d postgres
```

The Compose service stores database files in the `budget-postgres-data` named volume. Start the application with `.\mvnw.cmd spring-boot:run`; Flyway applies versioned SQL migrations from `src/main/resources/db/migration`, and Hibernate validates the resulting schema. The published host port defaults to `5432`; set `POSTGRES_PORT` to a different port if that port is already in use. The application uses the same `POSTGRES_PORT` value by default.

For local development, the database name, username, and password default to `budget`. Override them with `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` before starting Compose, and set matching `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` values for the application. Replace the development credentials with managed secrets in production.

Stop the database without deleting its records using `docker compose stop postgres` or `docker compose down`. Do not use `docker compose down --volumes` unless you intend to permanently delete the database.

## Feature summary

Monthly Budget
 - Include Transaction
 - Edit Transaction
 
Annual Budget

Stores
 - Unclassified
 - List Stores
 - Edit Store
 - Categories
  - List Categories
  - Edit Category
 - SubCategories
  - List Subcategories
  - Edit Subcategory

Reports

## Google sign-in

The application uses Google OAuth2 login and permits only the verified Google account whose email matches `app.admin-email` in `src/main/resources/application.properties`.

1. Create a Google OAuth 2.0 web application client and add `http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI.
2. Set the client credentials in the environment before starting the app. In PowerShell:

   ```powershell
   $env:GOOGLE_CLIENT_ID = "your-client-id"
   $env:GOOGLE_CLIENT_SECRET = "your-client-secret"
   .\mvnw.cmd spring-boot:run
   ```

Opening the app redirects unauthenticated visitors to the Google sign-in page. A successful Google sign-in is accepted only for the configured admin email; do not commit OAuth client credentials.

# Category
  - name: String
  - type: Enum{'Expense','Income'}
  
Ex:
Renda - Income
Despesas Domesticas - Expense

# SubCategory
  - name: String
  - category: Category
  
Ex:
ING - Renda
Aluguel - Despesas Domesticas

# Store
  - name: String
  - category: SubCategory
  
# Transaction
  - store: Store
  - date: Date
  - amount: Double
  - description: String
  
Ex:
ING - 23/12/2018 - 1000 - Wage/Salary

# Use Cases

## Upload file

- Upload the ING CSV file.
- Convert into Bean.
- Save into the database.

## Register Transaction

- Add new transctions manually.
