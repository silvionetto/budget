# Budget
Monthly Budget

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
ING - 

# Expense
  - month: Integer
  - year: Integer
  - planed: Double
  - actual: Double
  
# Income
