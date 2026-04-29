DROP TABLE IF EXISTS coffee_order_item;
DROP TABLE IF EXISTS coffee_order;
DROP TABLE IF EXISTS coffee_chat_session;
DROP TABLE IF EXISTS coffee_sensitive_word;
DROP TABLE IF EXISTS coffee_product;
-- ============================================================
-- 咖啡智能助手 - 数据库初始化脚本
-- 所有表结构与 Java 实体（domain 包）完全对齐
-- ============================================================

-- 产品表 (→ CoffeeProduct.java)
CREATE TABLE IF NOT EXISTS coffee_product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '产品名',
    category    VARCHAR(50)  NOT NULL DEFAULT 'COFFEE' COMMENT '品类: COFFEE/TEA/PASTRY/OTHER',
    description TEXT COMMENT '详细描述',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    image_url   VARCHAR(500) COMMENT '产品图片链接',
    alias       VARCHAR(500) COMMENT '别名，分号分隔，用于搜索匹配',
    ingredients VARCHAR(500) COMMENT '成分描述',
    stock       INT NOT NULL DEFAULT 0 COMMENT '库存',
    is_active   TINYINT NOT NULL DEFAULT 1 COMMENT '是否在售',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete   TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '产品表';

-- 订单表 (→ CoffeeOrder.java)
CREATE TABLE IF NOT EXISTS coffee_order (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(32) COMMENT '订单编号',
    session_id   VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id      VARCHAR(64) COMMENT '用户标识',
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/PREPARING/COMPLETED/CANCELLED',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    remark       TEXT COMMENT '备注',
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete    TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '订单表';

-- 订单明细表 (→ CoffeeOrderItem.java)
CREATE TABLE IF NOT EXISTS coffee_order_item (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT NOT NULL COMMENT '订单ID',
    product_id    BIGINT NOT NULL COMMENT '产品ID',
    product_name  VARCHAR(100) NOT NULL COMMENT '下单时产品名快照',
    quantity      INT NOT NULL DEFAULT 1,
    unit_price    DECIMAL(10,2) NOT NULL,
    subtotal      DECIMAL(10,2) COMMENT '小计',
    customization VARCHAR(200) COMMENT '定制: 少糖/加冰等',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '订单明细表';

-- 会话历史表 (→ CoffeeChatSession.java)
CREATE TABLE IF NOT EXISTS coffee_chat_session (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id         VARCHAR(64) NOT NULL UNIQUE COMMENT '会话唯一标识',
    user_id            VARCHAR(64) COMMENT '用户标识',
    title              VARCHAR(200) COMMENT '会话标题',
    intent             VARCHAR(32) COMMENT '最后识别意图',
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/CLOSED/TRANSFERRED',
    satisfaction_score INT COMMENT '满意度评分 1-5',
    create_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete          TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '会话历史表';

-- 敏感词表 (→ CoffeeSensitiveWord.java)
CREATE TABLE IF NOT EXISTS coffee_sensitive_word (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    word        VARCHAR(100) NOT NULL COMMENT '敏感词',
    severity    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete   TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '敏感词表';

-- ============================================================
-- 示例产品数据
-- ============================================================
INSERT INTO coffee_product (name, category, description, price, alias, ingredients, stock, is_active) VALUES
('经典拿铁',    'COFFEE',  '浓郁意式浓缩与丝滑牛奶的完美融合，口感醇厚顺滑',   28.00, '拿铁;latte;牛奶咖啡',         '意式浓缩咖啡、鲜牛奶',            100, 1),
('卡布奇诺',    'COFFEE',  '意式浓缩搭配绵密奶泡，层次分明，口感丰富',         26.00, 'cappuccino;奶泡咖啡',         '意式浓缩咖啡、鲜牛奶、奶泡',       80,  1),
('美式咖啡',    'COFFEE',  '纯正意式浓缩搭配热水，清爽纯粹，提神醒脑',         22.00, '美式;americano;黑咖啡',       '意式浓缩咖啡、热水',              120, 1),
('摩卡咖啡',    'COFFEE',  '巧克力与咖啡的甜蜜邂逅，加入鲜奶油，香浓可口',      32.00, '摩卡;mocha;巧克力咖啡',       '意式浓缩咖啡、巧克力酱、鲜牛奶、奶油', 60,  1),
('香草拿铁',    'COFFEE',  '经典拿铁加入天然香草糖浆，甜蜜芬芳',               30.00, '香草;vanilla latte',          '意式浓缩咖啡、鲜牛奶、香草糖浆',     70,  1),
('焦糖玛奇朵',  'COFFEE',  '香浓焦糖遇见顺滑拿铁，甜蜜与微苦的交织',           32.00, '焦糖;caramel macchiato;玛奇朵', '意式浓缩咖啡、鲜牛奶、焦糖酱',      65,  1),
('榛果拿铁',    'COFFEE',  '浓郁榛果风味搭配经典拿铁，坚果香气四溢',            30.00, '榛果;hazelnut latte',         '意式浓缩咖啡、鲜牛奶、榛果糖浆',     55,  1),
('冷萃咖啡',    'COFFEE',  '12小时低温慢萃，口感柔和顺滑，酸度低',              28.00, '冷萃;cold brew;冰咖啡',       '冷萃咖啡液',                     50,  1),
('抹茶拿铁',    'TEA',     '日本宇治抹茶与鲜牛奶的清新组合',                    28.00, '抹茶;matcha latte;绿茶拿铁',  '抹茶粉、鲜牛奶',                  75,  1),
('伯爵红茶',    'TEA',     '经典英式伯爵茶，佛手柑清香怡人',                    22.00, '红茶;earl grey;伯爵茶',       '伯爵红茶叶',                     90,  1),
('抹茶慕斯蛋糕', 'PASTRY',  '日式抹茶慕斯，口感绵密，抹茶清香',                  35.00, '抹茶蛋糕;蛋糕;甜点',           '抹茶粉、奶油芝士、淡奶油、饼干底',    30,  1),
('提拉米苏',    'PASTRY',  '经典意式甜点，马斯卡彭芝士与咖啡的完美组合',         38.00, '提拉米苏;tiramisu;意式甜点',   '马斯卡彭芝士、咖啡、手指饼干、可可粉', 25,  1),
('巧克力布朗尼', 'PASTRY',  '浓郁巧克力蛋糕，外酥内软',                          28.00, '布朗尼;brownie;巧克力蛋糕',    '黑巧克力、黄油、面粉、核桃',         20,  1),
('蓝莓马芬',    'PASTRY',  '新鲜蓝莓烘焙的松软蛋糕',                            22.00, '马芬;muffin;蓝莓;蛋糕',        '面粉、蓝莓、黄油、鸡蛋',            35,  1),
('牛角可颂',    'PASTRY',  '法式黄油可颂，层层酥脆',                            18.00, '可颂;croissant;牛角包;面包',   '面粉、黄油、酵母',                 40,  1);
