-- src/test/resources/schema.sql
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone_number VARCHAR(20),
    role VARCHAR(20),
    balance DOUBLE DEFAULT 0,
    frozen_balance DOUBLE DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    item_type VARCHAR(20),
    owner VARCHAR(100),
    starting_price DOUBLE,
    description TEXT,
    artist_name VARCHAR(100),
    release_date DATE,
    warranty INT,
    brand VARCHAR(100),
    mileage INT,
    vehicle_id_plate VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS auction_sessions (
    session_id VARCHAR(50) PRIMARY KEY,
    owner_id INT,
    item_id INT,
    starting_price DOUBLE,
    step_price DOUBLE,
    start_time DATETIME,
    end_time DATETIME,
    duration_days INT,
    extension_count INT DEFAULT 0,
    current_price DOUBLE,
    status VARCHAR(20),
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (item_id) REFERENCES items(item_id)
);

CREATE TABLE IF NOT EXISTS bids (
    id INT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(50),
    user_id INT,
    amount DOUBLE,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES auction_sessions(session_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS proxy_bids (
    id INT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(50),
    user_id INT,
    max_amount DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active TINYINT(1) DEFAULT 1,
    FOREIGN KEY (session_id) REFERENCES auction_sessions(session_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);