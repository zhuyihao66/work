-- 猜你喜欢标签对应的模拟数据
-- 商品ID从600开始，图片ID从6000开始
SET NAMES utf8mb4;
USE `secondhand_market`;

-- 为"猜你喜欢"标签创建匹配的商品数据
-- 这些商品的标题包含对应的关键词，用于模糊搜索

INSERT INTO `items` (`id`,`seller_id`,`category_id`,`title`,`description`,`price`,`currency`,`item_condition`,`status`,`quantity`,`sale_type`,`location_text`,`latitude`,`longitude`,`created_at`,`updated_at`) VALUES
  -- 手机相关（标题包含"手机"）
  (600,1,2,'华为 P50 Pro 手机 256GB','麒麟9000芯片，徕卡四摄，99新，全套配件',3999.00,'CNY','like_new','listed',1,'fixed','上海 · 黄浦区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (601,2,2,'一加 11 手机 12GB+256GB','骁龙8 Gen2，使用半年，成色很好',3299.00,'CNY','like_new','listed',1,'fixed','北京 · 朝阳区',39.9189,116.4074,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (602,3,2,'vivo X90 Pro+ 手机','拍照神器，蔡司镜头，使用4个月',4599.00,'CNY','like_new','listed',1,'fixed','深圳 · 南山区',22.5329,113.9344,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
  
  -- 电脑相关（标题包含"电脑"）
  (603,1,3,'联想 ThinkPad X1 Yoga 电脑','14寸翻转触屏，i7-1165G7，16GB内存，512GB固态',5999.00,'CNY','good','listed',1,'fixed','上海 · 徐汇区',31.1792,121.4384,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (604,2,3,'华硕 ROG 游戏本 电脑','RTX 3060显卡，R7-5800H，16GB内存，1TB固态',6999.00,'CNY','good','listed',1,'fixed','北京 · 海淀区',39.9593,116.2984,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (605,3,3,'Surface Pro 9 电脑 平板二合一','i5-1235U，8GB+256GB，带键盘和笔',4999.00,'CNY','like_new','listed',1,'fixed','深圳 · 福田区',22.5431,114.0579,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  
  -- 相机相关（标题包含"相机"）
  (606,1,23,'富士 X100V 相机 旁轴','23mm定焦镜头，使用1年，成色很好',8999.00,'CNY','good','listed',1,'fixed','上海 · 静安区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (607,2,23,'理光 GR3 相机 街拍神器','28mm定焦，APS-C画幅，便携小巧',5999.00,'CNY','like_new','listed',1,'fixed','北京 · 西城区',39.9042,116.3668,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (608,3,23,'奥林巴斯 OM-1 相机','M43画幅，防抖强大，使用半年',12999.00,'CNY','like_new','listed',1,'fixed','深圳 · 龙华区',22.6569,114.0295,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  
  -- 耳机相关（标题包含"耳机"）
  (609,1,24,'Bose QuietComfort 45 降噪耳机','头戴式无线降噪，使用3个月，几乎全新',1999.00,'CNY','like_new','listed',1,'fixed','上海 · 长宁区',31.2204,121.4252,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (610,2,24,'森海塞尔 Momentum 4 无线耳机','头戴式，音质出色，使用半年',2499.00,'CNY','good','listed',1,'fixed','北京 · 东城区',39.9042,116.4074,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (611,3,24,'Beats Studio Buds 真无线耳机','主动降噪，使用4个月，成色很好',799.00,'CNY','like_new','listed',1,'fixed','深圳 · 宝安区',22.5553,113.8831,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (612,1,24,'JBL TUNE 750BTNC 蓝牙耳机','头戴式降噪，续航长，使用1年',399.00,'CNY','good','listed',1,'fixed','上海 · 普陀区',31.2417,121.3925,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  
  -- 球鞋相关（标题包含"球鞋"）
  (613,2,35,'Nike Air Jordan 1 球鞋 42码','经典复刻，穿了几次，成色很好',1299.00,'CNY','like_new','listed',1,'fixed','北京 · 丰台区',39.8584,116.2874,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (614,3,35,'Adidas Yeezy 350 球鞋 43码','经典配色，使用半年，有正常磨损',1999.00,'CNY','good','listed',1,'fixed','深圳 · 龙岗区',22.7196,114.2514,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (615,1,35,'New Balance 993 球鞋 41码','美产元年版，成色很好，原盒在',899.00,'CNY','good','listed',1,'fixed','上海 · 虹口区',31.2646,121.5052,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (616,2,35,'Converse Chuck Taylor 球鞋 42码','经典款，高帮，使用1年，有正常磨损',299.00,'CNY','fair','listed',1,'fixed','北京 · 石景山区',39.9066,116.2229,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  
  -- 行李箱相关（标题包含"行李箱"）
  (617,3,36,'Rimowa 行李箱 20寸 铝镁合金','经典款，使用1年，有正常使用痕迹',2999.00,'CNY','good','listed',1,'fixed','深圳 · 盐田区',22.5533,114.2378,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (618,1,36,'新秀丽 行李箱 28寸 万向轮','大容量，适合长途旅行，使用2年',799.00,'CNY','good','listed',1,'fixed','上海 · 杨浦区',31.3046,121.5052,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (619,2,36,'小米 90分 行李箱 24寸','拉链款，静音轮，使用半年，几乎全新',399.00,'CNY','like_new','listed',1,'fixed','北京 · 昌平区',40.2181,116.2348,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (620,3,36,'ITO 行李箱 20寸 登机箱','四轮万向，轻量化设计，使用1年',599.00,'CNY','good','listed',1,'fixed','深圳 · 罗湖区',22.5431,114.1238,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  
  -- 演出票相关（标题包含"演出票"或"票"）
  (621,1,20,'周杰伦 演唱会门票 2张','2024年上海站，VIP区，原价转让',1999.00,'CNY','new','listed',2,'fixed','上海 · 黄浦区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
  (622,2,20,'五月天 演唱会门票 1张','2024年北京站，看台票，原价出',599.00,'CNY','new','listed',1,'fixed','北京 · 朝阳区',39.9189,116.4074,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (623,3,20,'音乐剧《猫》演出票 2张','2024年深圳站，前排座位，原价转让',899.00,'CNY','new','listed',2,'fixed','深圳 · 南山区',22.5329,113.9344,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (624,1,20,'话剧《暗恋桃花源》演出票','2024年上海站，中后排，原价出',399.00,'CNY','new','listed',1,'fixed','上海 · 徐汇区',31.1792,121.4384,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  
  -- 热卖相关（高性价比、热门商品，标题包含"热卖"或热门关键词）
  (625,2,2,'iPhone 15 Pro Max 热卖','256GB，深空黑色，使用1个月，几乎全新',7999.00,'CNY','like_new','listed',1,'fixed','北京 · 海淀区',39.9593,116.2984,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
  (626,3,3,'MacBook Pro 14寸 M3 热卖','10核CPU，18GB内存，512GB存储，使用2个月',9999.00,'CNY','like_new','listed',1,'fixed','深圳 · 福田区',22.5431,114.0579,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (627,1,24,'AirPods Pro 2代 热卖','降噪耳机，使用1个月，功能完美',1299.00,'CNY','like_new','listed',1,'fixed','上海 · 静安区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW());

-- 商品图片
INSERT INTO `item_images` (`id`,`item_id`,`image_url`,`sort_order`,`created_at`) VALUES
  (6000,600,'https://picsum.photos/seed/huawei_p50pro/800/600',0,NOW()),
  (6001,601,'https://picsum.photos/seed/oneplus11/800/600',0,NOW()),
  (6002,602,'https://picsum.photos/seed/vivo_x90pro/800/600',0,NOW()),
  (6003,603,'https://picsum.photos/seed/thinkpad_x1yoga/800/600',0,NOW()),
  (6004,604,'https://picsum.photos/seed/asus_rog/800/600',0,NOW()),
  (6005,605,'https://picsum.photos/seed/surface_pro9/800/600',0,NOW()),
  (6006,606,'https://picsum.photos/seed/fuji_x100v/800/600',0,NOW()),
  (6007,607,'https://picsum.photos/seed/ricoh_gr3/800/600',0,NOW()),
  (6008,608,'https://picsum.photos/seed/olympus_om1/800/600',0,NOW()),
  (6009,609,'https://picsum.photos/seed/bose_qc45/800/600',0,NOW()),
  (6010,610,'https://picsum.photos/seed/sennheiser_momentum4/800/600',0,NOW()),
  (6011,611,'https://picsum.photos/seed/beats_studiobuds/800/600',0,NOW()),
  (6012,612,'https://picsum.photos/seed/jbl_tune750/800/600',0,NOW()),
  (6013,613,'https://picsum.photos/seed/jordan1/800/600',0,NOW()),
  (6014,614,'https://picsum.photos/seed/yeezy350/800/600',0,NOW()),
  (6015,615,'https://picsum.photos/seed/newbalance993/800/600',0,NOW()),
  (6016,616,'https://picsum.photos/seed/converse_chuck/800/600',0,NOW()),
  (6017,617,'https://picsum.photos/seed/rimowa/800/600',0,NOW()),
  (6018,618,'https://picsum.photos/seed/samsonite_28/800/600',0,NOW()),
  (6019,619,'https://picsum.photos/seed/mi_90fen/800/600',0,NOW()),
  (6020,620,'https://picsum.photos/seed/ito_luggage/800/600',0,NOW()),
  (6021,621,'https://picsum.photos/seed/jay_concert/800/600',0,NOW()),
  (6022,622,'https://picsum.photos/seed/mayday_concert/800/600',0,NOW()),
  (6023,623,'https://picsum.photos/seed/cats_musical/800/600',0,NOW()),
  (6024,624,'https://picsum.photos/seed/play_ticket/800/600',0,NOW()),
  (6025,625,'https://picsum.photos/seed/iphone15pm/800/600',0,NOW()),
  (6026,626,'https://picsum.photos/seed/mbp14_m3/800/600',0,NOW()),
  (6027,627,'https://picsum.photos/seed/airpods_pro2_hot/800/600',0,NOW());

