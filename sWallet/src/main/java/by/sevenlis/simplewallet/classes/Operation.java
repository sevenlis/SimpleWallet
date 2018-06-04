package by.sevenlis.simplewallet.classes;

import java.util.Date;

public class Operation {
    private int id;
    private Date date;
    private Category category;
    private String comment;
    private double sum;
    private int typeCoef;
    
    public Operation(int id, Date date, Category category, String comment, double sum, int typeCoef) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.comment = comment;
        this.sum = sum;
        this.typeCoef = typeCoef;
    }
    
    public Operation() {
        this(-1,new Date(),null,"",0d,0);
    }
    
    public int getId() {
        return id;
    }
    
    public Date getDate() {
        return date;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public String getComment() {
        return comment;
    }
    
    public double getSum() {
        return sum;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public void setCategory(Category category) {
        this.category = category;
        this.typeCoef = category.getTypeCoef();
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public void setSum(double sum) {
        this.sum = sum;
    }
    
}
