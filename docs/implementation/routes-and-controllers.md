# Routes and controllers

This document maps application routes to the HTML pages they render and the data they load.

## Main controller
File: `src/main/kotlin/com/silvionetto/budget/HtmlController.kt`

The controller is responsible for page rendering and page-specific model binding.

## Route catalog

| Route | Method | Purpose |
| --- | --- | --- |
| `/` | `GET` | Home dashboard for the current year |
| `/year/{year}` | `GET` | Dashboard for a specific year |
| `/types` | `GET` | Type-based budget summary |
| `/categories` | `GET` | Lists all categories |
| `/categories/{name}` | `GET` | Loads one category and its transactions |
| `/subcategories/{type}/{category}/{name}` | `GET` | Displays a subcategory context |
| `/transactions/{category}/{year}/{month}` | `GET` | Lists all transactions in that category/month |
| `/store/{id}` | `GET` | Shows a store and edit form |
| `/store/{id}` | `POST` | Updates store name/category/subcategory |
| `/balance/{year}` | `GET` | Shows annual balance summary |
| `/reports/{year}` | `GET` | Reports view |

## Rendering responsibilities

### `/`
Loads:
- `previousYear`
- `year`
- `nextYear`
- `title`
- `budgets = budgetService.getBudget(year)`

Renders:
- `body`

### `/categories`
Loads:
- category list
- year navigation values

Renders:
- `categories`

### `/categories/{name}`
Loads:
- selected category
- subcategories
- transactions
- annual budget summary for the category

Renders:
- `category`

### `/transactions/{category}/{year}/{month}`
Loads:
- selected category
- all subcategories for that category
- filtered transactions for matching month/year

Renders:
- `transactions`

### `/store/{id}`
Loads:
- selected store
- category/subcategory context
- list of categories and subcategories of the same type
- transactions for that store

Renders:
- `store`

### `/balance/{year}`
Loads:
- `budgetService.getBalance(year)`

Renders:
- `balance`

## Navigation helpers
The controller adds year helpers:

- `getPreviousYear(year)`
- `getNextYear(year)`

These are used to move between yearly views in the UI.

## Related docs
- [System architecture](../architecture/system-architecture.md)
- [Budget calculation](budget-calculation.md)
- [Source map](../references/source-map.md)
