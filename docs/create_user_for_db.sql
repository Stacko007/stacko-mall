-- 创建数据库及用户的sql脚本(首先使用root用户创建数据库)
CREATE DATABASE IF NOT EXISTS stacko_mall
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- 允许远程任意ip访问
CREATE USER IF NOT EXISTS 'stacko_mall'@'%' IDENTIFIED BY 'Zxy@19860122';

GRANT ALL PRIVILEGES ON stacko_mall.* TO 'stacko_mall'@'%';
FLUSH PRIVILEGES;