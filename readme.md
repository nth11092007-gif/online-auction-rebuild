### 0. Thông tin nhóm
Các thành viên:
- Bùi Lê Minh - 25021868 (Nhóm trưởng)
- Lê Quang Dũng - 25021679
- Nguyễn Tiến Hải - 25021750
- Nguyễn Phúc Huy - 25021801


## 1. Giới thiệu bài toán và phạm vi hệ thống
Hệ thống đấu giá trực tuyến cho phép người dùng tham gia các phiên đấu giá (auction session) theo thời gian thực. Người dùng có thể tạo phiên đấu giá mới, đặt giá (bid), theo dõi diễn biến giá và quản lý lịch sử đấu giá. Hệ thống sử dụng kiến trúc Client‑Server, trong đó Server xử lý logic nghiệp vụ, quản lý dữ liệu và đồng bộ trạng thái tới tất cả Client thông qua cơ chế Observer.

Phạm vi:
- Phía Server: quản lý tài khoản, phiên đấu giá, xử lý bid, gửi cập nhật thời gian thực.
- Phía Client: giao diện đồ họa JavaFX cho phép đăng nhập, xem danh sách phiên, tham gia đấu giá, đặt bid và nhận cập nhật giá tức thì.

## 2. Công nghệ sử dụng và môi trường chạy
- **Ngôn ngữ**: Java (JDK 17+)
- **Giao diện**: JavaFX (OpenJFX 17)
- **Giao tiếp mạng**: Java Socket, JSON (Gson)
- **Cơ sở dữ liệu**: MySQL (có script khởi tạo)
- **Build tool**: Sử dụng Maven
- **Môi trường chạy**:
  - JDK 17 có tích hợp JavaFX (hoặc đã cấu hình JavaFX SDK riêng)
  - MySQL Server 8.0
  - Hệ điều hành: Windows / Linux / macOS

### Yêu cầu cài đặt:
<!--sẽ thêm vào sau -->

### 3. Cấu trúc thư mục chính
```
📦src
 ┣ 📂main
 ┃ ┣ 📂java
 ┃ ┃ ┣ 📂.vscode
 ┃ ┃ ┃ ┗ 📜settings.json
 ┃ ┃ ┣ 📂Controller
 ┃ ┃ ┃ ┣ 📜AuctionDetailController.java
 ┃ ┃ ┃ ┣ 📜CreateAuctionController.java
 ┃ ┃ ┃ ┣ 📜HomeAdminController.java
 ┃ ┃ ┃ ┣ 📜HomeController.java
 ┃ ┃ ┃ ┣ 📜ItemCardController.java
 ┃ ┃ ┃ ┣ 📜Launcher.java
 ┃ ┃ ┃ ┣ 📜LoginController.java
 ┃ ┃ ┃ ┣ 📜MainApp.java
 ┃ ┃ ┃ ┣ 📜ProfileController.java
 ┃ ┃ ┃ ┣ 📜RegisterController.java
 ┃ ┃ ┃ ┗ 📜UITest.java
 ┃ ┃ ┣ 📂dao
 ┃ ┃ ┃ ┣ 📜AuctionSessionDAO.java
 ┃ ┃ ┃ ┣ 📜AuctionSessionDAOImpl.java
 ┃ ┃ ┃ ┣ 📜BidDAO.java
 ┃ ┃ ┃ ┣ 📜BidDAOImpl.java
 ┃ ┃ ┃ ┣ 📜ItemDAO.java
 ┃ ┃ ┃ ┣ 📜ItemDAOImpl.java
 ┃ ┃ ┃ ┣ 📜UserDAO.java
 ┃ ┃ ┃ ┗ 📜UserDAOImpl.java
 ┃ ┃ ┣ 📂database
 ┃ ┃ ┃ ┗ 📜quan_ly_dau_gia.sql
 ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┣ 📜BidRequest.java
 ┃ ┃ ┃ ┗ 📜Message.java
 ┃ ┃ ┣ 📂Exception
 ┃ ┃ ┃ ┣ 📜PasswordStrengthCheck.java
 ┃ ┃ ┃ ┗ 📜UserExisted.java
 ┃ ┃ ┣ 📂factory
 ┃ ┃ ┃ ┣ 📜ItemsFactory.java
 ┃ ┃ ┃ ┣ 📜TypeArts.java
 ┃ ┃ ┃ ┣ 📜TypeElectronics.java
 ┃ ┃ ┃ ┗ 📜TypeVehicles.java
 ┃ ┃ ┣ 📂model
 ┃ ┃ ┃ ┣ 📂state
 ┃ ┃ ┃ ┃ ┣ 📜AuctionState.java
 ┃ ┃ ┃ ┃ ┣ 📜AuctionStateFactory.java
 ┃ ┃ ┃ ┃ ┣ 📜ClosedState.java
 ┃ ┃ ┃ ┃ ┣ 📜OpenState.java
 ┃ ┃ ┃ ┃ ┣ 📜PendingState.java
 ┃ ┃ ┃ ┃ ┗ 📜SettledState.java
 ┃ ┃ ┃ ┣ 📜Arts.java
 ┃ ┃ ┃ ┣ 📜AuctionSession.java
 ┃ ┃ ┃ ┣ 📜Bid.java
 ┃ ┃ ┃ ┣ 📜Bidder.java
 ┃ ┃ ┃ ┣ 📜Electronics.java
 ┃ ┃ ┃ ┣ 📜Item.java
 ┃ ┃ ┃ ┣ 📜ItemsAttributes.java
 ┃ ┃ ┃ ┣ 📜Seller.java
 ┃ ┃ ┃ ┣ 📜Transaction.java
 ┃ ┃ ┃ ┣ 📜User.java
 ┃ ┃ ┃ ┗ 📜Vehicles.java
 ┃ ┃ ┣ 📂server
 ┃ ┃ ┃ ┣ 📂command
 ┃ ┃ ┃ ┃ ┣ 📜Command.java
 ┃ ┃ ┃ ┃ ┣ 📜GetSessionsCommand.java
 ┃ ┃ ┃ ┃ ┣ 📜GetUserCommand.java
 ┃ ┃ ┃ ┃ ┣ 📜JoinSessionCommand.java
 ┃ ┃ ┃ ┃ ┣ 📜LoginCommand.java
 ┃ ┃ ┃ ┃ ┣ 📜PlaceBidCommand.java
 ┃ ┃ ┃ ┃ ┗ 📜SettleSessionCommand.java
 ┃ ┃ ┃ ┣ 📜AuctionFeedServer.java
 ┃ ┃ ┃ ┣ 📜AuctionServer.java
 ┃ ┃ ┃ ┣ 📜AuctionServerLauncher.java
 ┃ ┃ ┃ ┣ 📜AuctionWebSocketClient.java
 ┃ ┃ ┃ ┣ 📜Observer.java
 ┃ ┃ ┃ ┣ 📜ServerApp.java
 ┃ ┃ ┃ ┗ 📜WebSocketObserver.java
 ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┣ 📜AuctionEventPublisher.java
 ┃ ┃ ┃ ┣ 📜AuctionService.java
 ┃ ┃ ┃ ┣ 📜ServiceFactory.java
 ┃ ┃ ┃ ┣ 📜SettlementService.java
 ┃ ┃ ┃ ┗ 📜UserService.java
 ┃ ┃ ┣ 📂test
 ┃ ┃ ┃ ┗ 📜MainTest.java
 ┃ ┃ ┣ 📂utils
 ┃ ┃ ┃ ┣ 📜AppConfig.java
 ┃ ┃ ┃ ┣ 📜DBConnection.java
 ┃ ┃ ┃ ┣ 📜IDGenerator.java
 ┃ ┃ ┃ ┗ 📜SessionManager.java
 ┃ ┃ ┗ 📜SO_DO_LOP.png
 ┃ ┗ 📂resources
 ┃ ┃ ┣ 📂Images
 ┃ ┃ ┃ ┣ 📜background.jpg
 ┃ ┃ ┃ ┣ 📜BaseItem.png
 ┃ ┃ ┃ ┣ 📜logo.png
 ┃ ┃ ┃ ┗ 📜newBackground.png
 ┃ ┃ ┣ 📜ArtDetail.fxml
 ┃ ┃ ┣ 📜AuctionDetail.fxml
 ┃ ┃ ┣ 📜AuctionDetailSeller.fxml
 ┃ ┃ ┣ 📜config.properties
 ┃ ┃ ┣ 📜CreateAuction.fxml
 ┃ ┃ ┣ 📜ElectronicDetail.fxml
 ┃ ┃ ┣ 📜Home.fxml
 ┃ ┃ ┣ 📜HomeAdmin.fxml
 ┃ ┃ ┣ 📜ItemCard.fxml
 ┃ ┃ ┣ 📜Login.fxml
 ┃ ┃ ┣ 📜Profile.fxml
 ┃ ┃ ┣ 📜Register.fxml
 ┃ ┃ ┗ 📜VehicleDetail.fxml
 ┣ 📂test
 ┃ ┣ 📂java
 ┃ ┃ ┣ 📂dao
 ┃ ┃ ┃ ┗ 📜UserDAOImplTest.java
 ┃ ┃ ┣ 📂factory
 ┃ ┃ ┃ ┗ 📜TypeArtsTest.java
 ┃ ┃ ┣ 📂model
 ┃ ┃ ┃ ┗ 📜ItemsAttributesTest.java
 ┃ ┃ ┣ 📂server
 ┃ ┃ ┃ ┗ 📜AuctionServerTest.java
 ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┣ 📜AuctionServiceTest.java
 ┃ ┃ ┃ ┗ 📜SettlementServiceTest.java
 ┃ ┃ ┗ 📂utils
 ┃ ┃ ┃ ┗ 📜IDGeneratorTest.java
 ┃ ┣ 📂resources
 ┃ ┃ ┣ 📜quan_ly_dau_gia.sql
 ┃ ┃ ┗ 📜schema.sql
 ┃ ┗ 📜New Text Document.txt
 ┗ 📂testUI
 ┃ ┣ 📂.mvn
 ┃ ┃ ┗ 📂wrapper
 ┃ ┃ ┃ ┣ 📜maven-wrapper.jar
 ┃ ┃ ┃ ┗ 📜maven-wrapper.properties
 ┃ ┣ 📂src
 ┃ ┃ ┗ 📂main
 ┃ ┃ ┃ ┣ 📂java
 ┃ ┃ ┃ ┃ ┣ 📂com
 ┃ ┃ ┃ ┃ ┃ ┗ 📂example
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂testui
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HelloApplication.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┣ 📜HelloController.java
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜Launcher.java
 ┃ ┃ ┃ ┃ ┣ 📂vn
 ┃ ┃ ┃ ┃ ┃ ┗ 📂hnue
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂demo
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜App.java
 ┃ ┃ ┃ ┃ ┗ 📜module-info.java
 ┃ ┃ ┃ ┗ 📂resources
 ┃ ┃ ┃ ┃ ┗ 📂com
 ┃ ┃ ┃ ┃ ┃ ┗ 📂example
 ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📂testui
 ┃ ┃ ┃ ┃ ┃ ┃ ┃ ┗ 📜hello-view.fxml
 ┃ ┣ 📜.gitignore
 ┃ ┣ 📜mvnw
 ┃ ┣ 📜mvnw.cmd
 ┃ ┣ 📜pom.xml
 ┃ ┣ java -jar AuctionSystem-1.0-SNAPSHOT-server.jar
 ┃ ┗ java -jar AuctionSystem-1.0-SNAPSHOT-client.jar
```
### 4. Vị trí các file jar
- File SERVER.jar, file CLIENT.jar đều ở folder lớn, cùng với pom.xml.

### 5. Hướng dẫn chạy Server/Client cụ thể.

1. Đảm bảo đã có Java, JDK từ phiên bản 17 trở lên.
2. Thiết lập XAMPP phù hợp với hệ điều hành: https://www.apachefriends.org/
3. Khi cài xong XAMPP, bật Apache và MySQL lên. Đảm bảo Apache hiện xanh với ports 80, 443 và MySQL hiện xanh với port 3306.
4. Chạy java -jar AuctionSystem-1.0-SNAPSHOT-server.jar.
5. Chạy java -jar AuctionSystem-1.0-SNAPSHOT-client.jar.

### 6. Danh sách tính năng đã hoàn thành

- Đăng ký tài khoản: Tạo tài khoản mới với username, password, email, số dư ảo.

- Đăng nhập / Đăng xuất: Xác thực và quản lý phiên làm việc.

- Tạo phiên đấu giá: Người dùng có thể tạo phiên với tên sản phẩm, giá khởi điểm, thời gian kết thúc.

- Xem danh sách phiên đấu giá: Hiển thị tất cả phiên đang hoạt động (có thể lọc theo trạng thái).

- Tham gia phiên đấu giá: Xem chi tiết phiên, giá hiện tại, thời gian còn lại.

- Đặt giá (Bid): Người dùng nhập giá cao hơn giá hiện tại, hệ thống kiểm tra số dư và xác nhận bid.

- Cập nhật giá theo thời gian thực: Sử dụng Observer để tự động cập nhật giá và thời gian kết thúc phiên trên tất cả client khi có bid mới.

- Quản lý lịch sử đấu giá: Xem các bid đã đặt trong 1 phiên.

- Quản lý số dư: Trừ tiền khi đặt bid, cập nhật khi thắng đấu giá.

- Giao diện đồ họa JavaFX: Các màn hình đăng nhập, đăng ký, trang chủ, chi tiết phiên, lịch sử đều có giao diện trực quan.

- Xử lý lỗi cơ bản: Thông báo khi đặt bid không hợp lệ, số dư không đủ, phiên đã kết thúc.

### 7. Link video demo và báo cáo
- Link video: https://drive.google.com/file/d/1k3EjsiEjLnl_ttPlDir16CqU-3-aBw1L/view?usp=sharing
- Link báo cáo: https://drive.google.com/file/d/11-sM-k4tqYGrjABb6sAuuS32uRY-ZVSy/view?usp=sharing
