package com.chaxu.catalog;

import com.chaxu.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 骨架接口：静态样例；后续替换为 catalog 应用服务及 MyBatis 查询。 */
@RestController
public class CatalogController {
    public record Product(long id, String name, int priceCents, String category) {}
    @GetMapping("/api/products")
    public ApiResponse<List<Product>> products() {
        return ApiResponse.ok(List.of(
            new Product(1, "茉莉白月光", 1800, "原叶鲜奶茶"),
            new Product(2, "山野栀子青", 1800, "原叶鲜奶茶"),
            new Product(3, "蜜桃乌龙", 2200, "清爽鲜果茶"),
            new Product(4, "桂花慢乌龙", 2000, "原叶鲜奶茶"),
            new Product(5, "青提茉莉", 2400, "清爽鲜果茶"),
            new Product(6, "云雾知春", 1200, "轻盈纯茶")
        ));
    }
}
