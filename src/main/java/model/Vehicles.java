package model;
public class Vehicles extends Items{
    private final String brand;
    private final int mileage;
    private final String vehicleID;

    public Vehicles(int itemID, String ownerName, double startingPrice, String description, String brand, int mileage, String vehicleID) {
        super(itemID, ownerName, startingPrice, description);
        this.brand = brand;
        this.mileage = mileage;
        this.vehicleID = vehicleID;
    }

    public String getBrand() {
        return brand;
    }
    public String getVehicleID() {
        return vehicleID;
    }
    public int getMileage() {
        return mileage;
    }

    @Override
    public String showInfo() {
        return "Current Item: \nType: Vehicle\n Owner: " + getOwnerName() + "\nBrand: " + getBrand() + "\nLicense Plate: " + getVehicleID() + "\nMileage: " + getMileage() + "\nDescription: " + getDescription() + "\nStarting Price: " + getStartingPrice();
    }
}