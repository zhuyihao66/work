-- Items seed data for secondhand_market
-- 为所有分类创建模拟商品数据
-- 注意：商品ID从500开始，图片ID从5000开始，避免与现有数据冲突
SET NAMES utf8mb4;
USE `secondhand_market`;

-- Items (商品数据从ID 500开始，避免与现有数据冲突)
INSERT INTO `items` (`id`,`seller_id`,`category_id`,`title`,`description`,`price`,`currency`,`item_condition`,`status`,`quantity`,`sale_type`,`location_text`,`latitude`,`longitude`,`created_at`,`updated_at`) VALUES
  -- 数码3C - 手机 (category_id=2)
  (500,1,2,'小米 14 Pro 512GB','全新未拆封，朋友送的礼物，原价转手',4599.00,'CNY','new','listed',1,'fixed','上海 · 黄浦区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (501,2,2,'OPPO Find X6 Pro','使用半年，99新，无任何问题，全套配件',3299.00,'CNY','like_new','listed',1,'fixed','北京 · 朝阳区',39.9189,116.4074,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  -- 数码3C - 电脑 (category_id=3)
  (502,1,3,'MacBook Air M2 256GB','2023年款，使用一年，外观完美',6999.00,'CNY','like_new','listed',1,'fixed','上海 · 徐汇区',31.1792,121.4384,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (503,3,3,'Dell XPS 13 2022款','i7-1250U 16GB 512GB，办公用，成色很好',5499.00,'CNY','good','listed',1,'fixed','深圳 · 福田区',22.5431,114.0579,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  -- 数码3C - 平板 (category_id=22)
  (504,2,22,'iPad Pro 12.9寸 2021款','M1芯片，256GB，带Apple Pencil和键盘',5999.00,'CNY','like_new','listed',1,'fixed','北京 · 西城区',39.9042,116.3668,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  (505,1,22,'华为 MatePad Pro 11寸','鸿蒙系统，128GB，带手写笔',1999.00,'CNY','good','listed',1,'fixed','上海 · 静安区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  -- 数码3C - 相机 (category_id=23)
  (506,3,23,'佳能 EOS R6 Mark II','全画幅微单，快门次数约5000次，箱说全',18999.00,'CNY','like_new','listed',1,'fixed','深圳 · 南山区',22.5329,113.9344,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW()),
  (507,2,23,'索尼 A7M3','全画幅微单，使用正常，配原装电池充电器',12999.00,'CNY','good','listed',1,'fixed','北京 · 海淀区',39.9593,116.2984,DATE_SUB(NOW(), INTERVAL 9 DAY),NOW()),
  -- 数码3C - 影音设备 (category_id=24)
  (508,1,24,'AirPods Pro 2代','降噪耳机，使用3个月，功能正常',1299.00,'CNY','like_new','listed',1,'fixed','上海 · 长宁区',31.2204,121.4252,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
  (509,3,24,'索尼 WH-1000XM5','头戴式降噪耳机，99新，配件齐全',2299.00,'CNY','like_new','listed',1,'fixed','深圳 · 龙华区',22.6569,114.0295,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  -- 数码3C - 可穿戴设备 (category_id=25)
  (510,2,25,'Apple Watch Series 9','45mm GPS版，使用4个月，表带多套',2999.00,'CNY','like_new','listed',1,'fixed','北京 · 东城区',39.9042,116.4074,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (511,1,25,'华为 Watch GT 4','46mm，功能正常，成色很好',899.00,'CNY','good','listed',1,'fixed','上海 · 普陀区',31.2417,121.3925,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  -- 数码3C - 数码配件 (category_id=26)
  (512,3,26,'Anker 20000mAh 移动电源','支持快充，Type-C接口，使用半年',199.00,'CNY','good','listed',1,'fixed','深圳 · 宝安区',22.5553,113.8831,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (513,2,26,'Apple MagSafe 充电器','原装正品，全新未拆',299.00,'CNY','new','listed',1,'fixed','北京 · 丰台区',39.8584,116.2874,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
  -- 数码3C - 单反/微单 (category_id=41)
  (514,1,41,'尼康 D850','全画幅单反，快门次数约2万，成色很好',13999.00,'CNY','good','listed',1,'fixed','上海 · 虹口区',31.2646,121.5052,DATE_SUB(NOW(), INTERVAL 10 DAY),NOW()),
  (515,3,41,'富士 X-T5','APS-C画幅微单，带18-55mm镜头',8999.00,'CNY','like_new','listed',1,'fixed','深圳 · 龙岗区',22.7196,114.2514,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  -- 数码3C - 镜头 (category_id=42)
  (516,2,42,'佳能 RF 24-70mm f/2.8','全画幅镜头，使用正常，成色很好',8999.00,'CNY','good','listed',1,'fixed','北京 · 石景山区',39.9066,116.2229,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  (517,1,42,'索尼 FE 85mm f/1.8','人像镜头，成色完美，无霉无灰',2999.00,'CNY','like_new','listed',1,'fixed','上海 · 杨浦区',31.3046,121.5052,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  -- 数码3C - 录音/监听 (category_id=43)
  (518,3,43,'雅马哈 UR22C 声卡','USB-C接口，专业录音设备，功能正常',1299.00,'CNY','good','listed',1,'fixed','深圳 · 盐田区',22.5533,114.2378,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (519,2,43,'铁三角 ATH-M50X 监听耳机','专业监听耳机，使用1年，成色良好',799.00,'CNY','good','listed',1,'fixed','北京 · 昌平区',40.2181,116.2348,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  
  -- 居家生活 - 家具 (category_id=5)
  (520,1,5,'实木书桌 120cm','北欧风格，原木色，使用1年，成色很好',899.00,'CNY','good','listed',1,'fixed','上海 · 浦东新区',31.2244,121.5444,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (521,2,5,'宜家 布艺沙发','三人位，灰色，可拆洗，使用2年',1299.00,'CNY','good','listed',1,'fixed','北京 · 通州区',39.9097,116.6564,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW()),
  -- 居家生活 - 冰洗空 (category_id=27)
  (522,3,27,'海尔 变频冰箱 215L','二级能效，使用3年，制冷正常',899.00,'CNY','fair','listed',1,'fixed','深圳 · 罗湖区',22.5431,114.1238,DATE_SUB(NOW(), INTERVAL 9 DAY),NOW()),
  (523,1,27,'格力 1.5匹 变频空调','冷暖两用，使用2年，包安装',1999.00,'CNY','good','listed',1,'fixed','上海 · 闵行区',31.1124,121.3838,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  -- 居家生活 - 厨房电器 (category_id=28)
  (524,2,28,'美的 破壁机','多功能料理机，使用1年，功能正常',399.00,'CNY','good','listed',1,'fixed','北京 · 大兴区',39.7289,116.3384,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (525,3,28,'小米 电饭煲 3L','智能预约，使用半年，成色很好',199.00,'CNY','like_new','listed',1,'fixed','深圳 · 坪山区',22.6978,114.3464,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  -- 居家生活 - 清洁电器 (category_id=29)
  (526,1,29,'戴森 V8 吸尘器','无线手持，使用2年，续航正常',1599.00,'CNY','good','listed',1,'fixed','上海 · 嘉定区',31.3837,121.2625,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (527,2,29,'科沃斯 扫地机器人','自动回充，使用1年，功能正常',899.00,'CNY','good','listed',1,'fixed','北京 · 房山区',39.7355,116.1392,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  -- 居家生活 - 家居日用 (category_id=30)
  (528,3,30,'床上四件套 1.8米','纯棉材质，清洗过1次，几乎全新',199.00,'CNY','like_new','listed',1,'fixed','深圳 · 光明区',22.7489,113.9359,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (529,1,30,'北欧风格 装饰画套装','3幅画，无框画，搬家出',299.00,'CNY','good','listed',1,'fixed','上海 · 青浦区',31.1612,121.1245,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  -- 居家生活 - 收纳整理 (category_id=31)
  (530,2,31,'收纳箱 5件套','塑料材质，大中小号，搬家出',99.00,'CNY','good','listed',5,'fixed','北京 · 怀柔区',40.3158,116.6389,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
  (531,3,31,'衣柜收纳盒 布艺','可折叠，多个尺寸，使用1年',79.00,'CNY','good','listed',3,'fixed','深圳 · 大鹏新区',22.5959,114.4675,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  -- 居家生活 - 灯具照明 (category_id=32)
  (532,1,32,'LED 台灯 护眼','可调光调色，使用半年，功能正常',129.00,'CNY','like_new','listed',1,'fixed','上海 · 奉贤区',30.9180,121.4737,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (533,2,32,'北欧风格 吊灯','简约设计，三色光，使用1年',399.00,'CNY','good','listed',1,'fixed','北京 · 密云区',40.3769,116.8432,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  
  -- 服饰鞋包 - 男装 (category_id=33)
  (534,1,33,'优衣库 男士羽绒服 L码','黑色，穿了一季，成色很好',299.00,'CNY','good','listed',1,'fixed','上海 · 黄浦区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (535,2,33,'李维斯 牛仔裤 32码','经典款，使用半年，轻微磨损',199.00,'CNY','good','listed',1,'fixed','北京 · 朝阳区',39.9189,116.4074,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  -- 服饰鞋包 - 女装 (category_id=34)
  (536,3,34,'ZARA 女款大衣 M码','驼色，毛呢材质，穿了一季',399.00,'CNY','good','listed',1,'fixed','深圳 · 福田区',22.5431,114.0579,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (537,1,34,'H&M 连衣裙 M码','春夏款，只穿过一次，几乎全新',99.00,'CNY','like_new','listed',1,'fixed','上海 · 徐汇区',31.1792,121.4384,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  -- 服饰鞋包 - 鞋靴 (category_id=35)
  (538,2,35,'Nike Air Max 90 42码','经典跑鞋，使用半年，成色良好',499.00,'CNY','good','listed',1,'fixed','北京 · 西城区',39.9042,116.3668,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (539,3,35,'Dr. Martens 马丁靴 41码','经典款，使用1年，有使用痕迹',699.00,'CNY','fair','listed',1,'fixed','深圳 · 南山区',22.5329,113.9344,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  -- 服饰鞋包 - 箱包 (category_id=36)
  (540,1,36,'新秀丽 拉杆箱 20寸','万向轮，使用1年，有轻微划痕',899.00,'CNY','good','listed',1,'fixed','上海 · 静安区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (541,2,36,'Coach 女士手提包','真皮材质，使用2年，成色良好',1299.00,'CNY','good','listed',1,'fixed','北京 · 海淀区',39.9593,116.2984,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW()),
  
  -- 家用电器 (category_id=7)
  (542,3,7,'飞利浦 空气炸锅','5L容量，使用1年，功能正常',299.00,'CNY','good','listed',1,'fixed','深圳 · 龙华区',22.6569,114.0295,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (543,1,7,'九阳 豆浆机','1.2L容量，使用半年，成色很好',199.00,'CNY','like_new','listed',1,'fixed','上海 · 长宁区',31.2204,121.4252,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  
  -- 运动健康 - 运动装备 (category_id=37)
  (544,2,37,'Wilson 网球拍','专业拍，使用1年，成色很好',399.00,'CNY','good','listed',1,'fixed','北京 · 东城区',39.9042,116.4074,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (545,3,37,'Nike 篮球','标准7号球，使用半年',129.00,'CNY','good','listed',1,'fixed','深圳 · 宝安区',22.5553,113.8831,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  -- 运动健康 - 健身器材 (category_id=38)
  (546,1,38,'哑铃 20kg 可调节','一对装，使用1年，成色良好',299.00,'CNY','good','listed',1,'fixed','上海 · 普陀区',31.2417,121.3925,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (547,2,38,'瑜伽垫 加厚款','10mm厚度，使用半年，几乎全新',89.00,'CNY','like_new','listed',1,'fixed','北京 · 丰台区',39.8584,116.2874,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  
  -- 户外旅行 - 露营登山 (category_id=39)
  (548,3,39,'牧高笛 帐篷 双人','自动帐篷，使用2次，几乎全新',499.00,'CNY','like_new','listed',1,'fixed','深圳 · 龙岗区',22.7196,114.2514,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  (549,1,39,'登山杖 一对','铝合金材质，可调节长度，使用1年',199.00,'CNY','good','listed',1,'fixed','上海 · 虹口区',31.2646,121.5052,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  -- 户外旅行 - 骑行滑板 (category_id=40)
  (550,2,40,'捷安特 山地车','26寸，21速，使用2年，成色良好',1299.00,'CNY','good','listed',1,'fixed','北京 · 石景山区',39.9066,116.2229,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW()),
  (551,3,40,'滑板 专业板','七层枫木，使用半年，有正常磨损',299.00,'CNY','good','listed',1,'fixed','深圳 · 盐田区',22.5533,114.2378,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  
  -- 乐器音乐 (category_id=10)
  (552,1,10,'雅马哈 F310 吉他','民谣吉他，使用1年，成色很好',899.00,'CNY','good','listed',1,'fixed','上海 · 杨浦区',31.3046,121.5052,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (553,2,10,'卡西欧 PX-160 电钢琴','88键重锤，使用2年，功能正常',1999.00,'CNY','good','listed',1,'fixed','北京 · 昌平区',40.2181,116.2348,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  
  -- 游戏电竞 - 主机/掌机 (category_id=44)
  (554,3,44,'PlayStation 5','光驱版，使用1年，带2个手柄',2999.00,'CNY','good','listed',1,'fixed','深圳 · 罗湖区',22.5431,114.1238,DATE_SUB(NOW(), INTERVAL 9 DAY),NOW()),
  (555,1,44,'Nintendo Switch OLED','日版，使用半年，带保护套',1999.00,'CNY','like_new','listed',1,'fixed','上海 · 闵行区',31.1124,121.3838,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  -- 游戏电竞 - 游戏周边 (category_id=45)
  (556,2,45,'雷蛇 机械键盘','青轴，RGB背光，使用1年',399.00,'CNY','good','listed',1,'fixed','北京 · 大兴区',39.7289,116.3384,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (557,3,45,'罗技 G502 鼠标','游戏鼠标，可调重量，使用半年',299.00,'CNY','like_new','listed',1,'fixed','深圳 · 坪山区',22.6978,114.3464,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  
  -- 图书文创 - 文学艺术 (category_id=46)
  (558,1,46,'《百年孤独》马尔克斯','正版，阅读过1次，成色很好',29.00,'CNY','good','listed',1,'fixed','上海 · 嘉定区',31.3837,121.2625,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (559,2,46,'《三体》三部曲','科幻小说，正版，阅读过2次',89.00,'CNY','good','listed',1,'fixed','北京 · 房山区',39.7355,116.1392,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  -- 图书文创 - 教材教辅 (category_id=47)
  (560,3,47,'考研英语 真题集','2023年版，做过部分题目',39.00,'CNY','fair','listed',1,'fixed','深圳 · 光明区',22.7489,113.9359,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (561,1,47,'高等数学 教材','同济版，有笔记，成色良好',49.00,'CNY','good','listed',1,'fixed','上海 · 青浦区',31.1612,121.1245,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  -- 图书文创 - 漫画杂志 (category_id=48)
  (562,2,48,'《进击的巨人》1-10卷','正版日漫，保存良好',299.00,'CNY','good','listed',1,'fixed','北京 · 怀柔区',40.3158,116.6389,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (563,3,48,'《海贼王》单行本','1-20卷，正版，有轻微翻阅痕迹',599.00,'CNY','good','listed',1,'fixed','深圳 · 大鹏新区',22.5959,114.4675,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  
  -- 母婴用品 (category_id=13)
  (564,1,13,'婴儿推车 可折叠','高景观，使用半年，成色很好',599.00,'CNY','like_new','listed',1,'fixed','上海 · 奉贤区',30.9180,121.4737,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (565,2,13,'婴儿床 实木','可调节高度，使用1年，成色良好',899.00,'CNY','good','listed',1,'fixed','北京 · 密云区',40.3769,116.8432,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  
  -- 宠物用品 (category_id=14)
  (566,3,14,'猫爬架 三层','实木材质，使用1年，成色良好',299.00,'CNY','good','listed',1,'fixed','深圳 · 龙华区',22.6569,114.0295,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (567,1,14,'宠物自动喂食器','定时投食，使用半年，功能正常',199.00,'CNY','like_new','listed',1,'fixed','上海 · 黄浦区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  
  -- 出行交通 (category_id=15)
  (568,2,15,'电动车 新国标','48V 20Ah，使用1年，续航正常',1299.00,'CNY','good','listed',1,'fixed','北京 · 朝阳区',39.9189,116.4074,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  (569,3,15,'平衡车 小米','9号平衡车，使用2年，功能正常',899.00,'CNY','good','listed',1,'fixed','深圳 · 福田区',22.5431,114.0579,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  
  -- 办公设备 (category_id=16)
  (570,1,16,'惠普 激光打印机','黑白打印，使用1年，功能正常',899.00,'CNY','good','listed',1,'fixed','上海 · 徐汇区',31.1792,121.4384,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  (571,2,16,'明基 投影仪','1080P，使用2年，灯泡寿命充足',1999.00,'CNY','good','listed',1,'fixed','北京 · 西城区',39.9042,116.3668,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  
  -- 摄影影音 (category_id=17)
  (572,3,17,'大疆 DJI Mini 3','航拍无人机，使用半年，成色很好',3999.00,'CNY','like_new','listed',1,'fixed','深圳 · 南山区',22.5329,113.9344,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW()),
  (573,1,17,'GoPro HERO 11','运动相机，使用1年，功能正常',2499.00,'CNY','good','listed',1,'fixed','上海 · 静安区',31.2304,121.4737,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  
  -- 潮流玩具 (category_id=18)
  (574,2,18,'泡泡玛特 盲盒 全套','12个装，未拆封，全新',299.00,'CNY','new','listed',1,'fixed','北京 · 海淀区',39.9593,116.2984,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  (575,3,18,'乐高 街景系列','已拼装，带说明书，保存良好',1299.00,'CNY','good','listed',1,'fixed','深圳 · 龙岗区',22.7196,114.2514,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
  
  -- 手办模型 (category_id=19)
  (576,1,19,'万代 高达模型 RG','已拼装，带盒子，成色很好',199.00,'CNY','good','listed',1,'fixed','上海 · 虹口区',31.2646,121.5052,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
  (577,2,19,'手办 初音未来','正版，带盒子，保存良好',599.00,'CNY','good','listed',1,'fixed','北京 · 石景山区',39.9066,116.2229,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
  
  -- 票券卡类 (category_id=20)
  (578,3,20,'星巴克 咖啡券','面值500元，有效期3个月',450.00,'CNY','new','listed',1,'fixed','深圳 · 盐田区',22.5533,114.2378,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
  (579,1,20,'电影票 兑换券','5张装，有效期1年',199.00,'CNY','new','listed',1,'fixed','上海 · 杨浦区',31.3046,121.5052,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
  
  -- 其他 (category_id=21)
  (580,2,21,'二手书 杂项','各种书籍，打包出售',99.00,'CNY','fair','listed',10,'fixed','北京 · 昌平区',40.2181,116.2348,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
  (581,3,21,'闲置物品 大杂烩','各种小物件，搬家出',199.00,'CNY','good','listed',1,'fixed','深圳 · 罗湖区',22.5431,114.1238,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW());

-- Item Images (商品图片，从ID 5000开始)
INSERT INTO `item_images` (`id`,`item_id`,`image_url`,`sort_order`,`created_at`) VALUES
  (5000,500,'https://picsum.photos/seed/mi14pro/800/600',0,NOW()),
  (5001,501,'https://picsum.photos/seed/oppofindx6/800/600',0,NOW()),
  (5002,502,'https://picsum.photos/seed/mba_m2/800/600',0,NOW()),
  (5003,503,'https://picsum.photos/seed/dellxps13/800/600',0,NOW()),
  (5004,504,'https://picsum.photos/seed/ipadpro/800/600',0,NOW()),
  (5005,505,'https://picsum.photos/seed/matepad/800/600',0,NOW()),
  (5006,506,'https://picsum.photos/seed/canon_r6/800/600',0,NOW()),
  (5007,507,'https://picsum.photos/seed/sony_a7m3/800/600',0,NOW()),
  (5008,508,'https://picsum.photos/seed/airpods_pro2/800/600',0,NOW()),
  (5009,509,'https://picsum.photos/seed/sony_wh1000xm5/800/600',0,NOW()),
  (5010,510,'https://picsum.photos/seed/apple_watch9/800/600',0,NOW()),
  (5011,511,'https://picsum.photos/seed/huawei_watch_gt4/800/600',0,NOW()),
  (5012,512,'https://picsum.photos/seed/anker_powerbank/800/600',0,NOW()),
  (5013,513,'https://picsum.photos/seed/magsafe/800/600',0,NOW()),
  (5014,514,'https://picsum.photos/seed/nikon_d850/800/600',0,NOW()),
  (5015,515,'https://picsum.photos/seed/fuji_xt5/800/600',0,NOW()),
  (5016,516,'https://picsum.photos/seed/canon_rf2470/800/600',0,NOW()),
  (5017,517,'https://picsum.photos/seed/sony_fe85/800/600',0,NOW()),
  (5018,518,'https://picsum.photos/seed/yamaha_ur22c/800/600',0,NOW()),
  (5019,519,'https://picsum.photos/seed/ath_m50x/800/600',0,NOW()),
  (5020,520,'https://picsum.photos/seed/desk_wood/800/600',0,NOW()),
  (5021,521,'https://picsum.photos/seed/sofa_ikea/800/600',0,NOW()),
  (5022,522,'https://picsum.photos/seed/haier_fridge/800/600',0,NOW()),
  (5023,523,'https://picsum.photos/seed/gree_ac/800/600',0,NOW()),
  (5024,524,'https://picsum.photos/seed/midea_blender/800/600',0,NOW()),
  (5025,525,'https://picsum.photos/seed/mi_rice_cooker/800/600',0,NOW()),
  (5026,526,'https://picsum.photos/seed/dyson_v8/800/600',0,NOW()),
  (5027,527,'https://picsum.photos/seed/ecovacs_robot/800/600',0,NOW()),
  (5028,528,'https://picsum.photos/seed/bedding/800/600',0,NOW()),
  (5029,529,'https://picsum.photos/seed/decorative_art/800/600',0,NOW()),
  (5030,530,'https://picsum.photos/seed/storage_boxes/800/600',0,NOW()),
  (5031,531,'https://picsum.photos/seed/closet_organizer/800/600',0,NOW()),
  (5032,532,'https://picsum.photos/seed/led_lamp/800/600',0,NOW()),
  (5033,533,'https://picsum.photos/seed/ceiling_lamp/800/600',0,NOW()),
  (5034,534,'https://picsum.photos/seed/uniqlo_jacket/800/600',0,NOW()),
  (5035,535,'https://picsum.photos/seed/levis_jeans/800/600',0,NOW()),
  (5036,536,'https://picsum.photos/seed/zara_coat/800/600',0,NOW()),
  (5037,537,'https://picsum.photos/seed/hm_dress/800/600',0,NOW()),
  (5038,538,'https://picsum.photos/seed/nike_airmax/800/600',0,NOW()),
  (5039,539,'https://picsum.photos/seed/drmartens/800/600',0,NOW()),
  (5040,540,'https://picsum.photos/seed/samsonite_luggage/800/600',0,NOW()),
  (5041,541,'https://picsum.photos/seed/coach_bag/800/600',0,NOW()),
  (5042,542,'https://picsum.photos/seed/philips_airfryer/800/600',0,NOW()),
  (5043,543,'https://picsum.photos/seed/joyoung_soybean/800/600',0,NOW()),
  (5044,544,'https://picsum.photos/seed/wilson_tennis/800/600',0,NOW()),
  (5045,545,'https://picsum.photos/seed/nike_basketball/800/600',0,NOW()),
  (5046,546,'https://picsum.photos/seed/dumbbells/800/600',0,NOW()),
  (5047,547,'https://picsum.photos/seed/yoga_mat/800/600',0,NOW()),
  (5048,548,'https://picsum.photos/seed/tent_mobi/800/600',0,NOW()),
  (5049,549,'https://picsum.photos/seed/hiking_poles/800/600',0,NOW()),
  (5050,550,'https://picsum.photos/seed/giant_bike/800/600',0,NOW()),
  (5051,551,'https://picsum.photos/seed/skateboard/800/600',0,NOW()),
  (5052,552,'https://picsum.photos/seed/yamaha_guitar/800/600',0,NOW()),
  (5053,553,'https://picsum.photos/seed/casio_piano/800/600',0,NOW()),
  (5054,554,'https://picsum.photos/seed/ps5/800/600',0,NOW()),
  (5055,555,'https://picsum.photos/seed/switch_oled/800/600',0,NOW()),
  (5056,556,'https://picsum.photos/seed/razer_keyboard/800/600',0,NOW()),
  (5057,557,'https://picsum.photos/seed/logitech_g502/800/600',0,NOW()),
  (5058,558,'https://picsum.photos/seed/book_100years/800/600',0,NOW()),
  (5059,559,'https://picsum.photos/seed/book_threebody/800/600',0,NOW()),
  (5060,560,'https://picsum.photos/seed/book_kaoyan/800/600',0,NOW()),
  (5061,561,'https://picsum.photos/seed/book_math/800/600',0,NOW()),
  (5062,562,'https://picsum.photos/seed/comic_attack/800/600',0,NOW()),
  (5063,563,'https://picsum.photos/seed/comic_onepiece/800/600',0,NOW()),
  (5064,564,'https://picsum.photos/seed/baby_stroller/800/600',0,NOW()),
  (5065,565,'https://picsum.photos/seed/baby_bed/800/600',0,NOW()),
  (5066,566,'https://picsum.photos/seed/cat_tree/800/600',0,NOW()),
  (5067,567,'https://picsum.photos/seed/pet_feeder/800/600',0,NOW()),
  (5068,568,'https://picsum.photos/seed/ebike/800/600',0,NOW()),
  (5069,569,'https://picsum.photos/seed/balance_bike/800/600',0,NOW()),
  (5070,570,'https://picsum.photos/seed/hp_printer/800/600',0,NOW()),
  (5071,571,'https://picsum.photos/seed/benq_projector/800/600',0,NOW()),
  (5072,572,'https://picsum.photos/seed/dji_mini3/800/600',0,NOW()),
  (5073,573,'https://picsum.photos/seed/gopro_hero11/800/600',0,NOW()),
  (5074,574,'https://picsum.photos/seed/popmart/800/600',0,NOW()),
  (5075,575,'https://picsum.photos/seed/lego_city/800/600',0,NOW()),
  (5076,576,'https://picsum.photos/seed/gundam_rg/800/600',0,NOW()),
  (5077,577,'https://picsum.photos/seed/figure_miku/800/600',0,NOW()),
  (5078,578,'https://picsum.photos/seed/starbucks_card/800/600',0,NOW()),
  (5079,579,'https://picsum.photos/seed/movie_ticket/800/600',0,NOW()),
  (5080,580,'https://picsum.photos/seed/books_misc/800/600',0,NOW()),
  (5081,581,'https://picsum.photos/seed/misc_items/800/600',0,NOW());

