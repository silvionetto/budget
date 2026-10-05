# Domain model

This document describes the business entities used by the application and their relationships.

## Shared base entity
The JPA base class is `BaseEntity` in `src/main/kotlin/com/silvionetto/budget/Entities.kt`.

Fields:
- `id: Long?`
- `version: Long?`
- `lastUpdateDate: Date`

Every persisted entity inherits this behavior, including generated IDs and optimistic locking.

## User
`User` is stored in the `users` table.

Properties:
- `login`
- `firstname`
- `lastname`
- `description` (optional)

Purpose: identifies the application user or owner of the budget.

## BudgetCategory
`BudgetCategory` stores a financial category.

Properties:
- `name: String`
- `type: BudgetType`

`BudgetType` is an enum with:
- `EXPENSE`
- `INCOME`

Constraints:
- category name is unique

Usage:
- Example: `Renda` (income)
- Example: `Despesas Domesticas` (expense)

## BudgetSubCategory
`BudgetSubCategory` is the child classification inside a category.

Properties:
- `name: String`
- `category: BudgetCategory`

This creates a hierarchy:

Category -> Subcategory -> Store

## Store
`Store` is a merchant or account label associated with a subcategory.

Properties:
- `name: String`
- `subCategory: BudgetSubCategory`

Constraints:
- store name is unique

Example:
- `ING` under `Renda`
- `Aluguel` under `Despesas Domesticas`

## Transaction
`Transaction` represents a financial event.

Properties:
- `date: Date`
- `store: Store`
- `account: String`
- `contraAccount: String`
- `code: String`
- `debitCredit: String`
- `amount: Double`
- `transactionType: String`
- `notifications: String`
- `subCategory: BudgetSubCategory`

This is the primary record used for month-by-month financial reporting.

## Balance
`Balance` is a balance snapshot object.

Properties:
- `date: Date`
- `openingBalance: Double`

This is used as a summary record; the main reporting model is derived from transactions.

## Budget summary type
The non-persistent `Budget` class aggregates monthly values.

Properties:
- `category: String`
- `january` ... `december: Double`

Methods:
- `getTotal()`
- `getAverage()`

This is not stored in the database; it is created dynamically in the service layer for display.

## Relationship summary
The domain is structured as:

- User
  - owns the budget context
- Category
  - contains many subcategories
- Subcategory
  - contains many stores
- Store
  - has many transactions
- Transaction
  - belongs to a store and a subcategory

## Related docs
- [Project overview](../overview/project-overview.md)
- [System architecture](../architecture/system-architecture.md)
- [Source map](../references/source-map.md)
