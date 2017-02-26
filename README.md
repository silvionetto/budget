# budget
Monthly Budget

#Category
-name: String;
-type: Enum{'Expense','Income'}
#Transaction
-date: Date;
-amount: Double;
-description: String;
-category: Category;

#Expense
-month: Integer;
-year: Integer;
-planed: Double;
-actual: Double;
#Income
