# Project overview

## Purpose
This project is a personal budget tracker built with Spring Boot and Kotlin. It helps a user classify income and expenses, review monthly and yearly totals, and inspect transactions by category, subcategory, and store.

## Core domain
The app revolves around a simple accounting hierarchy:

- Category: large financial bucket such as income, transport, health, education
- Subcategory: finer-grained classification inside a category
- Store: the merchant or institution associated with a transaction
- Transaction: a financial event with date, amount, type, and notes

## Primary behaviors
- Track transactions in a database-backed model
- Group transactions by month and year
- Display category totals and annual summaries
- Drill down from a category or store to its transactions
- Render pages with server-side Thymeleaf templates

## Technical stack
- Kotlin
- Spring Boot
- Spring Data JPA
- Thymeleaf
- Spring Security with HTTP Basic auth
- JPA-backed relational persistence

## Main source folders
- `src/main/kotlin/com/silvionetto/budget` — application code
- `src/main/resources/templates` — HTML screens
- `src/main/resources` — configuration and properties
- `src/test/kotlin` — tests

## High-level workflow
1. Application starts and seeds default categories and users.
2. Transactions are created or imported into the system.
3. Services aggregate them into monthly and annual budget totals.
4. The controller populates models for the browser.
5. Thymeleaf renders the appropriate page.

## Related docs
- [Domain model](../domain/domain-model.md)
- [System architecture](../architecture/system-architecture.md)
- [Budget calculation](../implementation/budget-calculation.md)
