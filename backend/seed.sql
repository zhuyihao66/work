-- Seed data for secondhand_market
SET NAMES utf8mb4;
USE `secondhand_market`;

-- Clean tables (optional in dev)
-- ORDER matters because of logical refs
DELETE FROM `bids`;
DELETE FROM `auctions`;
DELETE FROM `shipments`;
DELETE FROM `payments`;
DELETE FROM `reviews`;
DELETE FROM `messages`;
DELETE FROM `order_items`;
DELETE FROM `orders`;
DELETE FROM `favorites`;
DELETE FROM `item_images`;
DELETE FROM `items`;
DELETE FROM `addresses`;
DELETE FROM `categories`;
DELETE FROM `users`;

-- Users
INSERT INTO `users` (`id`,`email`,`phone`,`password_hash`,`display_name`,`avatar_url`,`role`,`status`,`created_at`,`updated_at`) VALUES
  (1,'alice@example.com','13800138000','$2y$12$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa','Alice','https://picsum.photos/seed/alice/200','user','active',NOW(),NOW()),
  (2,'bob@example.com','13900139000','$2y$12$bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb','Bob','https://picsum.photos/seed/bob/200','user','active',NOW(),NOW()),
  (3,'charlie@example.com','13700137000','$2y$12$cccccccccccccccccccccccccccccccccccccccccccccccccccccccc','Charlie','https://picsum.photos/seed/charlie/200','user','active',NOW(),NOW()),
  (4,'admin@example.com','18600000000','$2y$12$dddddddddddddddddddddddddddddddddddddddddddddddddddddddd','Admin','https://picsum.photos/seed/admin/200','admin','active',NOW(),NOW());

-- Addresses
INSERT INTO `addresses` (`id`,`user_id`,`receiver_name`,`phone`,`country`,`province`,`city`,`district`,`address_line`,`postal_code`,`is_default`,`created_at`,`updated_at`) VALUES
  (1,1,'李静','13800138000','CN','上海','上海','浦东新区','张江高科技园区科苑路888号',200120,1,NOW(),NOW()),
  (2,2,'王磊','13900139000','CN','北京','北京','海淀区','中关村创业大街1号',100080,1,NOW(),NOW()),
  (3,3,'赵敏','13700137000','CN','广东','深圳','南山区','科苑南路2666号',518000,1,NOW(),NOW());

-- Categories
INSERT INTO `categories` (`id`,`name`,`parent_id`,`slug`,`created_at`) VALUES
  (1,'数码3C',NULL,'digital',NOW()),
  (2,'手机',1,'phones',NOW()),
  (3,'电脑',1,'computers',NOW()),
  (4,'居家生活',NULL,'home',NOW()),
  (5,'家具',4,'furniture',NOW());

-- Items (fixed price and auction)
INSERT INTO `items` (`id`,`seller_id`,`category_id`,`title`,`description`,`price`,`currency`,`item_condition`,`status`,`quantity`,`sale_type`,`location_text`,`latitude`,`longitude`,`created_at`,`updated_at`) VALUES
  (101,1,2,'iPhone 13 128GB 国行','自用一年，电池健康 87%，无暗病，原装配件齐全',3599.00,'CNY','good','listed',1,'fixed','上海 · 浦东新区',31.224361,121.544379,DATE_SUB(NOW(), INTERVAL 10 DAY),NOW()),
  (102,2,3,'ThinkPad X1 Carbon 2019','办公使用，外观轻微划痕，16G/512G，键盘手感好',2999.00,'CNY','like_new','listed',1,'fixed','北京 · 海淀区',39.9834,116.3229,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  (103,2,5,'宜家 可升降办公桌','尺寸 120x70cm，升降顺畅，自提优惠',499.00,'CNY','good','listed',1,'fixed','北京 · 海淀区',39.975,116.315,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (104,3,2,'小米 13 Ultra 拍卖','99新，带发票与保护壳，支持顺丰到付',1.00,'CNY','like_new','listed',1,'auction','深圳 · 南山区',22.5333,113.95,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW());

-- Item images
INSERT INTO `item_images` (`id`,`item_id`,`image_url`,`sort_order`,`created_at`) VALUES
  (1001,101,'https://picsum.photos/seed/iphone13a/800/600',0,NOW()),
  (1002,101,'https://picsum.photos/seed/iphone13b/800/600',1,NOW()),
  (1003,102,'https://picsum.photos/seed/x1c2019/800/600',0,NOW()),
  (1004,103,'https://picsum.photos/seed/desk/800/600',0,NOW()),
  (1005,104,'https://picsum.photos/seed/mi13u/800/600',0,NOW());

-- Favorites
INSERT INTO `favorites` (`user_id`,`item_id`,`created_at`) VALUES
  (2,101,DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (3,101,DATE_SUB(NOW(), INTERVAL 6 DAY)),
  (1,102,DATE_SUB(NOW(), INTERVAL 6 DAY));

-- Auctions
INSERT INTO `auctions` (`id`,`item_id`,`start_price`,`reserve_price`,`bid_increment`,`start_time`,`end_time`,`status`,`created_at`,`updated_at`) VALUES
  (9001,104,3000.00,3500.00,50.00,DATE_SUB(NOW(), INTERVAL 1 DAY),DATE_ADD(NOW(), INTERVAL 1 DAY),'running',NOW(),NOW());

-- Bids
INSERT INTO `bids` (`id`,`auction_id`,`bidder_id`,`amount`,`created_at`) VALUES
  (9101,9001,1,3050.00,DATE_SUB(NOW(), INTERVAL 22 HOUR)),
  (9102,9001,2,3100.00,DATE_SUB(NOW(), INTERVAL 20 HOUR)),
  (9103,9001,1,3300.00,DATE_SUB(NOW(), INTERVAL 18 HOUR)),
  (9104,9001,2,3450.00,DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- One fixed-price order (Alice buys Bob's ThinkPad)
INSERT INTO `orders` (`id`,`order_no`,`buyer_id`,`seller_id`,`status`,`total_amount`,`currency`,`note`,`created_at`,`updated_at`) VALUES
  (5001,'ORD20251103001',1,2,'paid',2999.00,'CNY','含原装电源',DATE_SUB(NOW(), INTERVAL 3 DAY),DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO `order_items` (`id`,`order_id`,`item_id`,`title_snapshot`,`price_snapshot`,`quantity`) VALUES
  (6001,5001,102,'ThinkPad X1 Carbon 2019',2999.00,1);

-- Shipment for that paid order
INSERT INTO `shipments` (`id`,`order_id`,`shipping_provider`,`tracking_no`,`status`,`shipped_at`,`delivered_at`,`created_at`,`updated_at`) VALUES
  (7001,5001,'SFExpress','SF1234567890','in_transit',DATE_SUB(NOW(), INTERVAL 2 DAY),NULL,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW());

-- Payment record (simulated succeeded)
INSERT INTO `payments` (`id`,`order_id`,`provider`,`provider_tx_id`,`amount`,`currency`,`status`,`raw_payload`,`created_at`) VALUES
  (8001,5001,'alipay','ALI20251103001',2999.00,'CNY','captured',JSON_OBJECT('mock','ok','paid_at',NOW()),DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Messages between buyer and seller
INSERT INTO `messages` (`id`,`item_id`,`sender_id`,`receiver_id`,`content`,`is_read`,`created_at`) VALUES
  (3001,102,1,2,'你好，请问电脑有没有维修记录？',0,DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (3002,102,2,1,'没有，正常使用，支持当面验机。',1,DATE_SUB(NOW(), INTERVAL 8 DAY)),
  (3003,101,2,1,'iPhone13 能否小刀到 3400？',0,DATE_SUB(NOW(), INTERVAL 4 DAY));

-- Reviews (after completion, example only)
-- Suppose the order later completed and both reviewed
INSERT INTO `reviews` (`id`,`order_id`,`reviewer_id`,`reviewee_id`,`rating`,`comment`,`created_at`) VALUES
  (4001,5001,1,2,5,'发货很快，机器很新，满意！',DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Additional realistic items to enrich listing pages
INSERT INTO `items` (`id`,`seller_id`,`category_id`,`title`,`description`,`price`,`currency`,`item_condition`,`status`,`quantity`,`sale_type`,`location_text`,`latitude`,`longitude`,`created_at`,`updated_at`) VALUES
  (105,1,2,'华为 P40 5G','屏幕有轻微划痕，功能正常，原盒在',1299.00,'CNY','fair','listed',1,'fixed','上海 · 浦东新区',31.224,121.545,DATE_SUB(NOW(), INTERVAL 9 DAY),NOW()),
  (106,3,3,'MacBook Pro 2017 13寸','电池循环 600 次，屏幕轻微涂层脱落',3699.00,'CNY','good','listed',1,'fixed','深圳 · 南山区',22.54,113.95,DATE_SUB(NOW(), INTERVAL 11 DAY),NOW());

INSERT INTO `item_images` (`id`,`item_id`,`image_url`,`sort_order`,`created_at`) VALUES
  (1006,105,'https://picsum.photos/seed/p40/800/600',0,NOW()),
  (1007,106,'https://picsum.photos/seed/mbp2017/800/600',0,NOW());

-- Done

