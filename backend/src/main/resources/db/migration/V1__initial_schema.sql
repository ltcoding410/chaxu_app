-- MySQL 8.4。先自行创建 utf8mb4 数据库，再由 Flyway 执行此迁移。
-- 金额单位：分；时间：UTC；应用显示按 Asia/Shanghai。
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone VARCHAR(32) NOT NULL UNIQUE,
  display_name VARCHAR(80) NOT NULL,
  password_hash VARCHAR(255) NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB;
CREATE TABLE roles (id BIGINT PRIMARY KEY AUTO_INCREMENT, code VARCHAR(32) NOT NULL UNIQUE);
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL, role_id BIGINT NOT NULL,
  PRIMARY KEY(user_id, role_id),
  FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(role_id) REFERENCES roles(id)
);
CREATE TABLE member_accounts (
  user_id BIGINT PRIMARY KEY, points BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  FOREIGN KEY(user_id) REFERENCES users(id)
);
CREATE TABLE points_ledger (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL,
  amount BIGINT NOT NULL, balance_after BIGINT NOT NULL,
  business_type VARCHAR(32) NOT NULL, business_id VARCHAR(64) NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL UNIQUE,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  INDEX idx_points_user_time(user_id, created_at),
  FOREIGN KEY(user_id) REFERENCES member_accounts(user_id)
);
CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80) NOT NULL, sort_order INT NOT NULL DEFAULT 0
);
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, category_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL, description VARCHAR(500), image_url VARCHAR(500),
  status VARCHAR(24) NOT NULL DEFAULT 'ON_SALE', sort_order INT NOT NULL DEFAULT 0,
  FOREIGN KEY(category_id) REFERENCES categories(id)
);
CREATE TABLE product_variants (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, product_id BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL, price_cents BIGINT NOT NULL,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY(product_id) REFERENCES products(id), CHECK(price_cents >= 0)
);
CREATE TABLE product_options (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, product_id BIGINT NOT NULL,
  group_name VARCHAR(32) NOT NULL, name VARCHAR(80) NOT NULL,
  price_cents BIGINT NOT NULL DEFAULT 0,
  FOREIGN KEY(product_id) REFERENCES products(id), CHECK(price_cents >= 0)
);
CREATE TABLE coupon_templates (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(120) NOT NULL,
  points_cost BIGINT NOT NULL, discount_cents BIGINT NOT NULL, minimum_cents BIGINT NOT NULL,
  valid_days INT NOT NULL DEFAULT 30, remaining_stock INT NOT NULL,
  per_user_limit INT NOT NULL DEFAULT 3, enabled BOOLEAN NOT NULL DEFAULT TRUE,
  starts_at TIMESTAMP(3) NULL, ends_at TIMESTAMP(3) NULL,
  CHECK(points_cost > 0), CHECK(discount_cents > 0),
  CHECK(minimum_cents >= discount_cents), CHECK(remaining_stock >= 0),
  CHECK(valid_days > 0), CHECK(per_user_limit > 0)
);
CREATE TABLE user_coupons (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, template_id BIGINT NOT NULL,
  name_snapshot VARCHAR(120) NOT NULL, discount_cents BIGINT NOT NULL, minimum_cents BIGINT NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'AVAILABLE',
  expires_at TIMESTAMP(3) NOT NULL, locked_order_id BIGINT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  INDEX idx_coupon_user_status(user_id, status, expires_at),
  FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(template_id) REFERENCES coupon_templates(id)
);
CREATE TABLE coupon_user_quotas (
  user_id BIGINT NOT NULL, template_id BIGINT NOT NULL, redeemed_count INT NOT NULL DEFAULT 0,
  PRIMARY KEY(user_id, template_id),
  FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(template_id) REFERENCES coupon_templates(id),
  CHECK(redeemed_count >= 0)
);
CREATE TABLE coupon_redemptions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, user_id BIGINT NOT NULL, request_id VARCHAR(64) NOT NULL,
  template_id BIGINT NOT NULL, coupon_id BIGINT NOT NULL UNIQUE, ledger_id BIGINT NOT NULL UNIQUE,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_redeem_request(user_id, request_id),
  FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(template_id) REFERENCES coupon_templates(id),
  FOREIGN KEY(coupon_id) REFERENCES user_coupons(id), FOREIGN KEY(ledger_id) REFERENCES points_ledger(id)
);
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_no VARCHAR(64) NOT NULL UNIQUE,
  request_id VARCHAR(64) NOT NULL, user_id BIGINT NOT NULL,
  status VARCHAR(24) NOT NULL DEFAULT 'PENDING_PAYMENT',
  total_cents BIGINT NOT NULL, discount_cents BIGINT NOT NULL DEFAULT 0, paid_cents BIGINT NOT NULL,
  earned_points BIGINT NOT NULL DEFAULT 0, coupon_id BIGINT NULL,
  pickup_code VARCHAR(24) NOT NULL, pickup_date DATE NOT NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), expires_at TIMESTAMP(3) NOT NULL,
  paid_at TIMESTAMP(3) NULL, version BIGINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_order_request(user_id, request_id), UNIQUE KEY uk_pickup_date(pickup_date, pickup_code),
  INDEX idx_order_status_time(status, created_at), INDEX idx_order_user_time(user_id, created_at),
  FOREIGN KEY(user_id) REFERENCES users(id), FOREIGN KEY(coupon_id) REFERENCES user_coupons(id),
  CHECK(total_cents >= 0), CHECK(discount_cents >= 0), CHECK(discount_cents <= total_cents),
  CHECK(paid_cents = total_cents - discount_cents)
);
ALTER TABLE user_coupons ADD CONSTRAINT fk_coupon_locked_order FOREIGN KEY(locked_order_id) REFERENCES orders(id);
CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
  variant_id BIGINT NOT NULL, name_snapshot VARCHAR(120) NOT NULL, spec_snapshot JSON NOT NULL,
  unit_price_cents BIGINT NOT NULL, quantity INT NOT NULL,
  FOREIGN KEY(order_id) REFERENCES orders(id), FOREIGN KEY(product_id) REFERENCES products(id),
  FOREIGN KEY(variant_id) REFERENCES product_variants(id),
  CHECK(unit_price_cents >= 0), CHECK(quantity > 0)
);
CREATE TABLE payment_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL,
  merchant_payment_no VARCHAR(64) NOT NULL UNIQUE, channel VARCHAR(24) NOT NULL,
  channel_transaction_id VARCHAR(128) NULL, amount_cents BIGINT NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'CNY', status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  confirmed_at TIMESTAMP(3) NULL, created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_channel_tx(channel, channel_transaction_id),
  FOREIGN KEY(order_id) REFERENCES orders(id), CHECK(amount_cents >= 0)
);
CREATE TABLE refund_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, order_id BIGINT NOT NULL, payment_id BIGINT NOT NULL,
  request_id VARCHAR(64) NOT NULL UNIQUE, channel_refund_id VARCHAR(128) NULL UNIQUE,
  amount_cents BIGINT NOT NULL, status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
  reason VARCHAR(500), confirmed_at TIMESTAMP(3) NULL,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  FOREIGN KEY(order_id) REFERENCES orders(id), FOREIGN KEY(payment_id) REFERENCES payment_records(id),
  CHECK(amount_cents > 0)
);
CREATE TABLE audit_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT, actor_user_id BIGINT NULL,
  action VARCHAR(64) NOT NULL, target_type VARCHAR(64) NOT NULL, target_id VARCHAR(64),
  result VARCHAR(24) NOT NULL, detail JSON,
  created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  INDEX idx_audit_actor_time(actor_user_id, created_at), FOREIGN KEY(actor_user_id) REFERENCES users(id)
);
