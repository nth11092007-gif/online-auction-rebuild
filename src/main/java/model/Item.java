package model;
//Lớp trừu tượng đại diện cho một món hàng cơ bản trong hệ thống đấu giá.

public abstract class Item {
    //Tích hợp tự động sinh UUID hoặc lấy ID từ Database sau.
    protected int itemID;
    protected String ownerName;
    protected double startingPrice;
    protected String description;


    public Item(int itemID, String ownerName, double startingPrice, String description) {
        this.itemID = itemID;
        this.ownerName = ownerName;
        this.startingPrice = startingPrice;
        this.description = description;
    }

    public void setItemID(int itemID) { this.itemID = itemID; }
    public int getItemID() { return itemID; }
    public String getOwnerName() {
        return ownerName;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public String getDescription(){
        return description;
    }

    public abstract String showInfo();

    public String getItemType() {
        return this.getClass().getSimpleName();
    }
}