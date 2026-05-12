-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 19, 2026 at 11:56 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

-- ĐOẠN CODE THÊM MỚI ĐỂ FIX LỖI:
-- Tắt kiểm tra khóa ngoại để tránh lỗi khi xóa bảng
SET FOREIGN_KEY_CHECKS = 0; 

-- Xóa các bảng cũ nếu chúng đã tồn tại (Xóa từ bảng con đến bảng cha)
DROP TABLE IF EXISTS `bids`;
DROP TABLE IF EXISTS `auction_sessions`;
DROP TABLE IF EXISTS `items`;
DROP TABLE IF EXISTS `users`;

-- Bật lại kiểm tra khóa ngoại
SET FOREIGN_KEY_CHECKS = 1;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `quan_ly_dau_gia`
--

-- --------------------------------------------------------

--
-- Table structure for table `auction_sessions`
--

CREATE TABLE `auction_sessions` (
  `session_id` varchar(50) NOT NULL,
  `owner_id` int(11) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `starting_price` double DEFAULT NULL,
  `step_price` double DEFAULT NULL,
  `duration_days` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `bids`
--

CREATE TABLE `bids` (
  `id` int(11) NOT NULL,
  `session_id` varchar(50) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `bid_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE `items` (
  `item_id` int(11) NOT NULL,
  `item_type` varchar(50) DEFAULT NULL,
  `owner` varchar(100) DEFAULT NULL,
  `starting_price` double DEFAULT NULL,
  `description` text DEFAULT NULL,
  `artist_name` varchar(100) DEFAULT NULL,
  `release_date` date DEFAULT NULL,
  `warranty` int(11) DEFAULT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `mileage` int(11) DEFAULT NULL,
  `vehicle_id_plate` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `items`
--

INSERT INTO `items` (`item_id`, `item_type`, `owner`, `starting_price`, `description`, `artist_name`, `release_date`, `warranty`, `brand`, `mileage`, `vehicle_id_plate`) VALUES
(1, 'Arts', 'seller_minh', 1500, 'Bức tranh sơn dầu phong cảnh mùa thu cổ điển.', 'Danh họa Trần Anh', '2023-05-20', NULL, NULL, NULL, NULL),
(2, 'Electronics', 'seller_minh', 800, 'Laptop MacBook Air M2 2022, máy mới 99%.', NULL, NULL, 12, 'Apple', NULL, NULL),
(3, 'Vehicles', 'admin01', 25000, 'Xe điện Tesla Model 3 màu trắng, chạy êm.', NULL, NULL, NULL, 'Tesla', 5000, '30A-12345');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `real_name` varchar(100) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `role` varchar(20) DEFAULT NULL,
  `balance` double DEFAULT 0,
  `frozen_balance` double DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `email`, `phone_number`, `role`, `balance`, `frozen_balance`) VALUES
(1, 'admin01', '123456', 'Quản Trị Viên', 'admin@auction.com', '0912345678', 'ADMIN', 0, 0),
(2, 'seller_minh', '123456', 'Nguyễn Bình Minh', 'minh.seller@gmail.com', '0988776655', 'USER', 1000, 0),
(3, 'buyer_an', '123456', 'Trần Văn An', 'an.buyer@gmail.com', '0900112233', 'USER', 5000, 500);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `auction_sessions`
--
ALTER TABLE `auction_sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD KEY `owner_id` (`owner_id`),
  ADD KEY `item_id` (`item_id`);

--
-- Indexes for table `bids`
--
ALTER TABLE `bids`
  ADD PRIMARY KEY (`id`),
  ADD KEY `session_id` (`session_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `items`
--
ALTER TABLE `items`
  ADD PRIMARY KEY (`item_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bids`
--
ALTER TABLE `bids`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `auction_sessions`
--
ALTER TABLE `auction_sessions`
  ADD CONSTRAINT `auction_sessions_ibfk_1` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `auction_sessions_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `items` (`item_id`);

--
-- Constraints for table `bids`
--
ALTER TABLE `bids`
  ADD CONSTRAINT `bids_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `auction_sessions` (`session_id`),
  ADD CONSTRAINT `bids_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
