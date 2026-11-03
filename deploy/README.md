# 部署配置入口

本阶段不占用或绑定正式服务端口，不创建数据库或启动 Nginx。

1. 运行前端 `npm run build`，把生成的 `dist/` 放到静态目录。
2. 将 `nginx.conf.template` 复制为自己的 `.conf`，填写 6 个 `__...__` 占位符；`proxy_pass` 保留无尾斜杠写法，避免移除 `/api`。
3. 按 backend/.env.example 注入数据库参数和 SERVER_PORT，启用 mysql profile。Flyway 仅初始化表，不负责创建数据库。
4. 后端构建：`mvn clean package`，运行：`java -jar target/chaxu-api-0.1.0.jar`。
5. 用 `nginx -t` 检查填写后的配置，通过后再按你的环境启动或重载。

正式部署还需要完成服务端登录、支付和业务持久化，配置 HTTPS、Secure 会话 Cookie、数据库备份与密钥；当前浏览器演示不应作为真实收银系统上线。

配置地址与端口均由你自行填写。Docker Compose 等数据库编排待确认已有基础服务后再补，避免覆盖你的现有服务。
