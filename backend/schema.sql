-- MySQL 8 schema for Online Second-hand Marketplace
-- Charset/Collation: utf8mb4 for full Unicode support (emoji, CJK)

-- Safe defaults
SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET sql_notes = 0;

-- Create database
CREATE DATABASE IF NOT EXISTS `secondhand_market`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE `secondhand_market`;

-- Ensure strict modes
SET SESSION sql_mode = 'STRICT_ALL_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- Users
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` VARCHAR(255) NOT NULL COMMENT '邮箱',
  `phone` VARCHAR(32) NULL COMMENT '手机号',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `display_name` VARCHAR(100) NOT NULL COMMENT '显示昵称',
  `avatar_url` VARCHAR(512) NULL COMMENT '头像地址',
  `role` ENUM('user','admin') NOT NULL DEFAULT 'user' COMMENT '角色：user/admin',
  `status` ENUM('active','suspended','deleted') NOT NULL DEFAULT 'active' COMMENT '状态：active/suspended/deleted',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  KEY `idx_users_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- Auctions (拍卖主表)
CREATE TABLE IF NOT EXISTS `auctions` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` BIGINT UNSIGNED NOT NULL COMMENT '关联商品ID',
  `start_price` DECIMAL(10,2) NOT NULL COMMENT '起拍价',
  `reserve_price` DECIMAL(10,2) NULL COMMENT '保留价（可为空）',
  `bid_increment` DECIMAL(10,2) NOT NULL DEFAULT 1.00 COMMENT '最小加价幅度',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `status` ENUM('scheduled','running','ended','cancelled') NOT NULL DEFAULT 'scheduled' COMMENT '拍卖状态',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_auctions_item` (`item_id`),
  KEY `idx_auctions_status` (`status`),
  KEY `idx_auctions_time` (`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='拍卖表';

-- Bids (出价记录)
CREATE TABLE IF NOT EXISTS `bids` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `auction_id` BIGINT UNSIGNED NOT NULL COMMENT '拍卖ID',
  `bidder_id` BIGINT UNSIGNED NOT NULL COMMENT '出价人用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '出价金额',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出价时间',
  PRIMARY KEY (`id`),
  KEY `idx_bids_auction` (`auction_id`),
  KEY `idx_bids_bidder` (`bidder_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出价记录表';

-- Categories (simple parent-child)
CREATE TABLE IF NOT EXISTS `categories` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT UNSIGNED NULL COMMENT '父分类ID',
  `slug` VARCHAR(120) NOT NULL COMMENT '分类唯一标识',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categories_slug` (`slug`),
  KEY `idx_categories_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';

-- Items (listings)
CREATE TABLE IF NOT EXISTS `items` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID',
  `category_id` BIGINT UNSIGNED NULL COMMENT '分类ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `description` TEXT NULL COMMENT '描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `item_condition` ENUM('new','like_new','good','fair','poor') NOT NULL DEFAULT 'good' COMMENT '成色',
  `status` ENUM('listed','reserved','sold','archived') NOT NULL DEFAULT 'listed' COMMENT '状态',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '数量',
  `sale_type` ENUM('fixed','auction') NOT NULL DEFAULT 'fixed' COMMENT '售卖类型：一口价/拍卖',
  `location_text` VARCHAR(255) NULL COMMENT '位置描述',
  `latitude` DECIMAL(10,7) NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) NULL COMMENT '经度',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_items_seller` (`seller_id`),
  KEY `idx_items_category` (`category_id`),
  KEY `idx_items_status` (`status`),
  KEY `idx_items_sale_type` (`sale_type`),
  FULLTEXT KEY `ft_items_title_desc` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品/帖子表';

-- Item Images
CREATE TABLE IF NOT EXISTS `item_images` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `image_url` VARCHAR(512) NOT NULL COMMENT '图片URL',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_images_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品图片表';

-- Favorites (Wish list)
CREATE TABLE IF NOT EXISTS `favorites` (
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `item_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`item_id`),
  KEY `idx_favorites_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏表';

-- Favorite Stores (收藏的店)
CREATE TABLE IF NOT EXISTS `favorite_stores` (
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`user_id`,`seller_id`),
  KEY `idx_favorite_stores_seller` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏的店表';

-- Browse History (我的足迹)
CREATE TABLE IF NOT EXISTS `browse_history` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `item_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `viewed_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  KEY `idx_browse_history_user` (`user_id`),
  KEY `idx_browse_history_item` (`item_id`),
  KEY `idx_browse_history_user_time` (`user_id`, `viewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='浏览历史表';

-- Orders
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(40) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT '买家用户ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家用户ID',
  `status` ENUM('pending','paid','shipped','completed','cancelled','refunded') NOT NULL DEFAULT 'pending' COMMENT '订单状态',
  `total_amount` DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `note` VARCHAR(255) NULL COMMENT '备注',
  `expires_at` DATETIME NULL COMMENT '超时自动取消时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_order_no` (`order_no`),
  KEY `idx_orders_buyer` (`buyer_id`),
  KEY `idx_orders_seller` (`seller_id`),
  KEY `idx_orders_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- Order Items (snapshot of item data at purchase)
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `item_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `title_snapshot` VARCHAR(200) NOT NULL COMMENT '下单时标题快照',
  `price_snapshot` DECIMAL(10,2) NOT NULL COMMENT '下单时单价快照',
  `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_items_order` (`order_id`),
  KEY `idx_order_items_item` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单明细表';

-- Messages (simple direct messaging around an item)
CREATE TABLE IF NOT EXISTS `messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` BIGINT UNSIGNED NULL COMMENT '关联商品ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者用户ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者用户ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_messages_item` (`item_id`),
  KEY `idx_messages_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内私信表';

-- Addresses (for shipping if needed)
CREATE TABLE IF NOT EXISTS `addresses` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(100) NOT NULL COMMENT '收件人姓名',
  `phone` VARCHAR(32) NOT NULL COMMENT '收件人电话',
  `country` VARCHAR(64) NOT NULL DEFAULT 'CN' COMMENT '国家/地区',
  `province` VARCHAR(64) NULL COMMENT '省份',
  `city` VARCHAR(64) NULL COMMENT '城市',
  `district` VARCHAR(64) NULL COMMENT '区/县',
  `address_line` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(20) NULL COMMENT '邮编',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否默认地址',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_addresses_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收货地址表';

-- Reviews (buyer/seller feedback)
CREATE TABLE IF NOT EXISTS `reviews` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `reviewer_id` BIGINT UNSIGNED NOT NULL COMMENT '评价人用户ID',
  `reviewee_id` BIGINT UNSIGNED NOT NULL COMMENT '被评用户ID',
  `rating` TINYINT UNSIGNED NOT NULL COMMENT '评分1-5（应用层校验1-5）',
  `comment` VARCHAR(500) NULL COMMENT '评价内容',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_reviews_order` (`order_id`),
  KEY `idx_reviews_reviewee` (`reviewee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价表';

-- Payments (record of payment attempts)
CREATE TABLE IF NOT EXISTS `payments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `provider` ENUM('manual','alipay','wechat','stripe','paypal') NOT NULL DEFAULT 'manual' COMMENT '支付渠道',
  `provider_tx_id` VARCHAR(100) NULL COMMENT '渠道交易号',
  `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `status` ENUM('initiated','authorized','captured','failed','refunded') NOT NULL DEFAULT 'initiated' COMMENT '支付状态',
  `raw_payload` JSON NULL COMMENT '原始回调/请求数据',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payments_provider_tx` (`provider`,`provider_tx_id`),
  KEY `idx_payments_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付记录表';

-- Shipments (物流发货)
CREATE TABLE IF NOT EXISTS `shipments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `shipping_provider` VARCHAR(50) NOT NULL COMMENT '物流承运商',
  `tracking_no` VARCHAR(64) NOT NULL COMMENT '运单号',
  `status` ENUM('pending','shipped','in_transit','delivered','returned','lost') NOT NULL DEFAULT 'pending' COMMENT '物流状态',
  `shipped_at` DATETIME NULL COMMENT '发货时间',
  `delivered_at` DATETIME NULL COMMENT '签收时间',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shipments_order` (`order_id`),
  KEY `idx_shipments_tracking` (`tracking_no`),
  KEY `idx_shipments_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物流发货表';

-- Safe migration helpers for existing databases（如需迁移旧库请手动执行相应 ALTER）

SET sql_notes = 1;


