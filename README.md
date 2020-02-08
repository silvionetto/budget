# Budget
Monthly Budget
 - Include Transaction
 - Edit Transaction
Annual Budget
Reports

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
