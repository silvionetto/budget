# Budget calculation flow

This document describes how the app turns transactions into month-by-month budget values.

## Step 1: fetch transactions
The service layer queries `TransactionRepository` for date ranges and filters such as:

- `findByDebitCreditAndDateBetween`
- `findBySubCategoryCategoryAndDateBetween`
- `findBySubCategory`
- `findByStore`

## Step 2: normalize by month
For each transaction, the service uses Java `Calendar` to determine the transaction month:

- January = 1
- February = 2
- ...
- December = 12

Then it adds the amount to the correct month field in a `Budget` object.

## Step 3: aggregate by category or type
`BudgetService` contains the main budgeting logic.

### Income and expense totals
`getIncome(year)` and `getExpense(year)`:
- build a start date of `yyyy-01-01`
- build an end date of `yyyy+1-01-01`
- filter transactions by `Credit` or `Debit`
- sum per month
- return a complete `Budget`

### Category totals
`getBudget(year, category)`:
- loads all transactions for that category in the selected year
- sums the amount per month
- returns a `Budget` for the category

### Year-level summary
`getBudget(year)` iterates every category and builds one budget row per category.

## Step 4: compute derived metrics
`Budget.getTotal()` sums the 12 months.

`Budget.getAverage()` divides the total by 12.

The app also calculates a cash-flow style balance:

- `yield = income - expense`
- `projected balance` = running monthly net position

This is done in `getBalance(year)`, which carries forward the monthly difference across the year.

## Important implementation note
The app does not persist monthly totals as a separate reporting table. Instead, totals are computed at runtime from transaction records. That makes the reporting logic straightforward but means the budget is derived data, not a distinct stored model.

## Related docs
- [Domain model](../domain/domain-model.md)
- [Routes and controllers](routes-and-controllers.md)
- [Source map](../references/source-map.md)
