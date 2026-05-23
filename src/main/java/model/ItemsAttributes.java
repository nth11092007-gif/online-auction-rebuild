package model;

import java.time.LocalDate;
/*
 * Lớp này chứa các thuộc tính chung cho tất cả các loại mặt hàng
 * và các thuộc tính riêng biệt cho từng loại mặt hàng (Arts, Electronics, Vehicles).
 * Sử dụng Builder Pattern để tạo đối tượng một cách linh hoạt và
 * dễ dàng mở rộng trong tương lai nếu cần thêm loại mặt hàng mới hoặc thuộc tính mới.
 */
public class ItemsAttributes {
    // Thuoc tinh chung (Immutable)
    private final User owner;
    private final String itemName;
    private final double startingPrice;
    private final String description;

    // Thuoc tinh rieng cho Arts
    private final String artistName;
    private final LocalDate releaseDate;

    // Thuoc tinh rieng cho Electronics / Vehicles
    private final int warranty;
    private final String brand;
    private final int mileage;
    private final String vehicleID;

    // Private constructor: Chỉ Builder mới có quyền gọi
    private ItemsAttributes(Builder builder) {
        this.owner = builder.owner;
        this.itemName = builder.itemName;
        this.startingPrice = builder.startingPrice;
        this.description = builder.description;
        this.artistName = builder.artistName;
        this.releaseDate = builder.releaseDate;
        this.warranty = builder.warranty;
        this.brand = builder.brand;
        this.mileage = builder.mileage;
        this.vehicleID = builder.vehicleID;
    }

    // Getters (Không có Setters để đảm bảo an toàn dữ liệu)
    public User getOwner() { return owner; }
    public String getItemName() { return itemName; }
    public String getOwnerName() { return owner != null ? owner.getUsername() : "Unknown"; }
    public double getStartingPrice() { return startingPrice; }
    public String getDescription() { return description; }
    public String getArtistName() { return artistName; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public int getWarranty() { return warranty; }
    public String getBrand() { return brand; }
    public int getMileage() { return mileage; }
    public String getVehicleID() { return vehicleID; }

    // --- Static Inner Class Builder ---
    public static class Builder {
        // Required parameters (Các thuộc tính bắt buộc)
        private final User owner;
        private final String itemName;
        private final double startingPrice;

        // Optional parameters (Các thuộc tính tùy chọn - khởi tạo giá trị mặc định)
        private String description = "";
        private String artistName = null;
        private LocalDate releaseDate = null;
        private int warranty = 0;
        private String brand = null;
        private int mileage = 0;
        private String vehicleID = null;

        // Constructor cho các tham số bắt buộc
        public Builder(User owner, double startingPrice, String itemName) {
            this.owner = owner;
            this.startingPrice = startingPrice;
            this.itemName = itemName;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder artistName(String artistName) {
            this.artistName = artistName;
            return this;
        }

        public Builder releaseDate(LocalDate releaseDate) {
            this.releaseDate = releaseDate;
            return this;
        }

        public Builder warranty(int warranty) {
            this.warranty = warranty;
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder mileage(int mileage) {
            this.mileage = mileage;
            return this;
        }

        public Builder vehicleID(String vehicleID) {
            this.vehicleID = vehicleID;
            return this;
        }

        // Phương thức cuối cùng để tạo object
        public ItemsAttributes build() {
            return new ItemsAttributes(this);
        }
    }
}