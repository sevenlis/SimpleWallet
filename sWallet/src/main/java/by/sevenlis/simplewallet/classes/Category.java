package by.sevenlis.simplewallet.classes;

import android.os.Parcel;
import android.os.Parcelable;

import by.sevenlis.simplewallet.R;

public class Category implements Parcelable {
    private int id;
    private int typeCoef;
    private String name;
    private int imageRes;
    
    public Category(int id, int typeCoef, String name) {
        this.id = id;
        this.setTypeCoef(typeCoef);
        this.name = name;
    }
    
    public Category() {
        this(-1,0,"");
    }
    
    private Category(Parcel in) {
        id = in.readInt();
        typeCoef = in.readInt();
        name = in.readString();
        imageRes = in.readInt();
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setTypeCoef(int typeCoef) {
        this.typeCoef = typeCoef;
        if (this.typeCoef == -1) {
            this.imageRes = R.drawable.ic_minus;
        } else if (this.typeCoef == 1) {
            this.imageRes = R.drawable.ic_plus;
        } else {
            this.imageRes = R.drawable.ic_add_row;
        }
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getTypeCoef() {
        return typeCoef;
    }
    
    public String getName() {
        return name;
    }
    
    public int getId() {
        return id;
    }
    
    public int getImageRes() {
        return imageRes;
    }
    
    public boolean isExpense() {
        return this.typeCoef < 0;
    }
    
    public boolean isIncome() {
        return this.typeCoef > 0;
    }
    
    public boolean isDefault() {
        return this.id <= 2;
    }
    
    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }
        
        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(typeCoef);
        parcel.writeString(name);
        parcel.writeInt(imageRes);
    }
}
