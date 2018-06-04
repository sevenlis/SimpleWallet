package by.sevenlis.simplewallet.classes;

import java.util.List;

public class ExpListGroup {
    private Category category;
    private List<Operation> operations;
    private double total;
    
    public ExpListGroup(Category category, List<Operation> operations) {
        this.setCategory(category);
        this.setOperations(operations);
    }
    
    public int getCount() {
        return operations.size();
    }
    
    public Operation getOperation(int position) {
        return getOperations().get(position);
    }
    
    public Category getCategory() {
        return this.category;
    }
    
    public List<Operation> getOperations() {
        return this.operations;
    }
    
    public double getTotal() {
        return total;
    }
    
    private void setCategory(Category category) {
        this.category = category;
    }
    
    private void setOperations(List<Operation> operations) {
        this.operations = operations;
        double total = 0;
        for (Operation operation : operations) {
            total += operation.getSum();
        }
        setTotal(total);
    }
    
    private void setTotal(double total) {
        this.total = total;
    }
}
