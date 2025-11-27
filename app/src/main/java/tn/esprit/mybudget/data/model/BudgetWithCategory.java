package tn.esprit.mybudget.data.model;

public class BudgetWithCategory {
    public int budgetId;
    public int categoryId;
    public String categoryName;
    public double limitAmount;
    public String period;

    public BudgetWithCategory(int budgetId, int categoryId, String categoryName, double limitAmount, String period) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.limitAmount = limitAmount;
        this.period = period;
    }
}
