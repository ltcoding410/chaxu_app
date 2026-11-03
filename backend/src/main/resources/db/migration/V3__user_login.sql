-- 保留 users 的业务外键，新增账号登录；旧手机号用户可用原手机号作为账号。
ALTER TABLE users MODIFY COLUMN phone VARCHAR(32) NULL;
ALTER TABLE users ADD COLUMN username VARCHAR(50) NULL;
UPDATE users SET username = phone WHERE username IS NULL;
ALTER TABLE users MODIFY COLUMN username VARCHAR(50) NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE (username);
