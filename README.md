# 茶序 CHAXU · 奶茶店点单系统

Vue 3 + TypeScript + Pinia + Vue Router 的顾客端与门店工作台，以及 Java 21 / Spring Boot 后端骨架。按 `项目实施方案.md` 实现第一版框架。注册登录已接入 Spring Boot 与 MySQL，购物、订单、积分和券仍为按账号隔离的本地模拟数据，无真实支付。详见 [注册登录与本地联调](docs/注册登录与本地联调.md)。

## 启动界面

注册登录需要先运行 mysql 模式的后端（`SERVER_PORT=8080`）；Vite 默认将 `/api` 代理到该端口，完整启动参数见上面的联调文档。

需要 Node.js 20.19+ 或 22.12+（本地验证使用 Node.js 24）。

```powershell
npm install
npm run dev
```

打开终端显示的 Local 地址。未填写 FRONTEND_PORT 时由 Vite 使用默认空闲端口。路由：

- `/`：商品分类、搜索、规格定制、购物袋与模拟结算。
- `/me`：顾客“我的”个人中心，汇总积分、优惠券、订单与取餐说明。
- `/member`：从“我的”进入的会员权益页，包含积分兑换、券包与积分流水。
- `/orders`：订单筛选、取餐码、制作状态与再次点单。
- `/login`：顾客和员工共用的注册登录页，注册仅分配顾客角色。
- `/staff/login`：独立店员入口，提供登录及只读工作台浏览。
- `/admin`：公开只读工作台布局，操作本地演示数据需要 STAFF / ADMIN 权限。

体验流程：选饮品并加入购物袋 → 去结算 → 确认模拟下单 → 会员中心查看积分或兑换券 → 下一单使用优惠券 → 工作台推进制作和取餐。数据按当前浏览器保存，兑换券有效期 30 天，每种最多兑换 3 张。

顾客导航固定为“在线点单 / 我的订单 / 我的”，不提供工作台入口。门店人员单独访问 `/staff/login`。账号身份使用服务端 Session；游客可浏览页面，购物时引导登录，门店操作验证员工权限。

前后端联调前可复制 `.env.example` 为 `.env.local`，按需填写前端端口和后端代理地址。界面已能独立运行，不需要先配置 MySQL 或 Nginx。

## 后端

需要 Java 21 与 Maven 3.6.3+。

```powershell
cd backend
mvn test
mvn spring-boot:run
```

默认 demo profile 不连接数据库，后端端口不填写时随机分配，查看启动日志获取地址。GET `/api/health` 和 `/api/products` 可访问。demo 下账号接口返回未启用提示。mysql 模式支持注册登录，后台 API 按角色保护，具体业务边界见 [接口与接入说明](docs/接口与接入说明.md)。

mysql profile 必须先自行创建 utf8mb4 数据库，并通过 IDE 或系统环境变量设置 `SPRING_PROFILES_ACTIVE=mysql`、`DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 和 `SERVER_PORT`。`backend/.env.example` 只是说明文件，不自动读取。Flyway 执行 V1 表结构、V2 初始角色/商品/券模板及 V3 登录账号迁移；不会导入浏览器演示数据。

## 文件结构

```text
apps/web/src/
  components/     茶杯视觉组件
  pages/          点单、会员、订单、管理端页面
  stores/         本地演示业务状态
  data/           商品与券模板
  styles.css      统一设计变量及响应式样式
backend/
  src/main/java/com/chaxu/
    auth/ catalog/ ordering/ payment/ membership/ coupon/ reporting/ common/
  src/main/resources/db/migration/
deploy/           待填写的 Nginx 模板与部署说明
docs/             接口与后续接入边界
```

首期将顾客端与后台放在一个前端工程的不同路由，复用状态和设计系统；后续可以拆成两个独立应用，不影响后端模块边界。使用自定义轻量组件实现视觉设计，暂未引入 Vant / Element Plus，避免两套组件库增加首期体积。

## 构建与检查

```powershell
npm run build
cd backend
mvn test
```

本次验收细节与尚未接入的功能见 [框架验收记录](docs/框架验收记录.md)。

`DESIGN.md` 记录页面视觉和交互约定。Nginx 监听端口、服务器名称、静态文件目录、后端地址与端口均在 `deploy/nginx.conf.template` 中以占位符保留，填写并检查后才可使用。当前阶段不配置真实支付或修改本机已有数据库/Nginx 服务。
