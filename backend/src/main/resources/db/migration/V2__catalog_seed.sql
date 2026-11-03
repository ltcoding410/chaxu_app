-- 只初始化角色、饮品和券模板；不创建默认管理员，不导入浏览器体验积分。
INSERT INTO roles(id, code) VALUES (1,'CUSTOMER'),(2,'STAFF'),(3,'ADMIN');
INSERT INTO categories(id, name, sort_order) VALUES (1,'原叶鲜奶茶',1),(2,'清爽鲜果茶',2),(3,'轻盈纯茶',3);
INSERT INTO products(id, category_id, name, description, sort_order) VALUES
  (1,1,'茉莉白月光','横县茉莉 · 清甜鲜奶 · 轻盈花香',1),
  (2,1,'山野栀子青','栀子绿茶 · 牧场鲜奶 · 一口山野',2),
  (3,2,'蜜桃乌龙','白桃果肉 · 高山乌龙 · 鲜甜回甘',3),
  (4,1,'桂花慢乌龙','金桂花香 · 炭焙乌龙 · 醇厚奶香',4),
  (5,2,'青提茉莉','鲜剥青提 · 茉莉茶底 · 清新有层次',5),
  (6,3,'云雾知春','高山原叶 · 现萃清茶 · 自然回甘',6);
INSERT INTO product_variants(product_id,name,price_cents) VALUES
  (1,'标准杯',1800),(1,'加大杯',2100),(2,'标准杯',1800),(2,'加大杯',2100),
  (3,'标准杯',2200),(3,'加大杯',2500),(4,'标准杯',2000),(4,'加大杯',2300),
  (5,'标准杯',2400),(5,'加大杯',2700),(6,'标准杯',1200),(6,'加大杯',1500);
INSERT INTO product_options(product_id,group_name,name,price_cents)
  SELECT p.id, opts.group_name, opts.name, opts.price_cents FROM products p CROSS JOIN (
    SELECT '温度' group_name,'正常冰' name,0 price_cents UNION ALL SELECT '温度','少冰',0
    UNION ALL SELECT '温度','去冰',0 UNION ALL SELECT '温度','热饮',0
    UNION ALL SELECT '甜度','标准糖',0 UNION ALL SELECT '甜度','五分糖',0
    UNION ALL SELECT '甜度','三分糖',0 UNION ALL SELECT '甜度','不另加糖',0
    UNION ALL SELECT '加料','不加料',0 UNION ALL SELECT '加料','珍珠',200
  ) opts;
INSERT INTO coupon_templates(name,points_cost,discount_cents,minimum_cents,valid_days,remaining_stock,per_user_limit)
  VALUES ('日常小确幸',100,500,2000,30,1000,3),('好茶与你分享',180,1000,3500,30,500,3);
