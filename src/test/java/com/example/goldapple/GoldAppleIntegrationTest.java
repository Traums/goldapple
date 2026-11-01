package com.example.goldapple;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class GoldAppleIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getProducts_returnsCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].sku").value("LL-SERUM-30"))
                .andExpect(jsonPath("$[0].name").value("Сыворотка с витамином C, 30 мл"))
                .andExpect(jsonPath("$[0].imageUrl", containsString("images.unsplash.com")))
                .andExpect(jsonPath("$[6].status").value("OUT_OF_STOCK"))
                .andExpect(jsonPath("$[7].status").value("ARCHIVED"));
    }

    @Test
    void getProduct_unknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void getCustomer_returnsCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/customers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Petrov"))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.birthDate").value(nullValue()));
    }

    @Test
    void getCustomer_unknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void getOrders_returnsCustomerOrders() throws Exception {
        mockMvc.perform(get("/api/v1/customers/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));

        mockMvc.perform(get("/api/v1/customers/3/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOrder_returnsItems() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(4270.00))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productName").value("Сыворотка с витамином C, 30 мл"))
                .andExpect(jsonPath("$.items[0].price").value(2290.00))
                .andExpect(jsonPath("$.items[0].lineAmount").value(2290.00));
    }

    @Test
    void getOrder_unknown_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void createOrder_success() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": 2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/orders/")))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.totalAmount").value(1081.00))
                .andExpect(jsonPath("$.items[0].productName").value("Мицеллярная вода, 400 мл"))
                .andExpect(jsonPath("$.items[0].lineAmount").value(1081.00));

        mockMvc.perform(get("/api/v1/products/10"))
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    void createOrder_lastItem_marksProductOutOfStock() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 6, "quantity": 1}]}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products/6"))
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));
    }

    @Test
    void createOrder_blockedCustomer_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 2, "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_BLOCKED"))
                .andExpect(jsonPath("$.message").value("Клиент заблокирован"));
    }

    @Test
    void createOrder_archivedProduct_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 8, "quantity": 1}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_ARCHIVED"));
    }

    @Test
    void createOrder_outOfStock_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 7, "quantity": 1}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_OUT_OF_STOCK"));
    }

    @Test
    void createOrder_insufficientStock_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 6, "quantity": 2}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void createOrder_duplicateProduct_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": 1}, {"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DUPLICATE_PRODUCT"));
    }

    @Test
    void createOrder_emptyItems_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void createOrder_negativeQuantity_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": -1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void checkOrder_allowed() throws Exception {
        mockMvc.perform(post("/api/v1/orders/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.reason").value(nullValue()))
                .andExpect(jsonPath("$.totalAmount").value(540.50));
    }

    @Test
    void checkOrder_insufficientStock() throws Exception {
        mockMvc.perform(post("/api/v1/orders/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 5, "quantity": 4}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.totalAmount").value(nullValue()));
    }

    @Test
    void checkOrder_blockedCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/orders/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 2, "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("CUSTOMER_BLOCKED"));
    }

    @Test
    void cancelOrder_newOrder_restoresStock() throws Exception {
        mockMvc.perform(post("/api/v1/orders/3/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/v1/products/6"))
                .andExpect(jsonPath("$.stockQuantity").value(2));
        mockMvc.perform(get("/api/v1/products/9"))
                .andExpect(jsonPath("$.stockQuantity").value(62));
    }

    @Test
    void cancelOrder_paidOrder_returns409() throws Exception {
        mockMvc.perform(post("/api/v1/orders/2/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_CANNOT_BE_CANCELLED"));
    }

    @Test
    void apiDocs_areAvailableInRussian() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Золотое яблоко API"))
                .andExpect(jsonPath("$.paths['/api/v1/orders'].post.summary").value("Создать заказ"));

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void updateCustomer_blocked_cannotCreateOrder() throws Exception {
        mockMvc.perform(patch("/api/v1/customers/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Maria", "lastName": "Sokolova", "phone": "+79035556677", "status": "BLOCKED", "birthDate": "1992-04-18"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.birthDate").value("1992-04-18"));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 3, "items": [{"productId": 1, "quantity": 1}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CUSTOMER_BLOCKED"));
    }

    @Test
    void listOrders_andChangeStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(207)))
                .andExpect(jsonPath("$[0].id").value(207));

        mockMvc.perform(patch("/api/v1/orders/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PAID"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(patch("/api/v1/orders/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "NEW"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_STATUS_NOT_ALLOWED"));

        mockMvc.perform(patch("/api/v1/orders/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void createAndUpdateProduct() throws Exception {
        String location = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "LL-CREAM-50",
                                  "name": "Крем для лица, 50 мл",
                                  "brand": "Lumière Lab",
                                  "category": "SKINCARE",
                                  "price": 1290,
                                  "stockQuantity": 12,
                                  "status": "ACTIVE",
                                  "imageUrl": "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=900&q=80"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("LL-CREAM-50"))
                .andExpect(jsonPath("$.price").value(1290.00))
                .andExpect(header().string("Location", containsString("/api/v1/products/")))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "LL-SERUM-30",
                                  "name": "Дубль",
                                  "brand": "Lumière Lab",
                                  "category": "SKINCARE",
                                  "price": 10,
                                  "stockQuantity": 1,
                                  "status": "ACTIVE",
                                  "imageUrl": "https://example.com/serum.jpg"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_SKU_TAKEN"));

        mockMvc.perform(patch(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sku": "LL-CREAM-50",
                                  "name": "Крем для лица, 40 мл",
                                  "brand": "Lumière Lab",
                                  "category": "MAKEUP",
                                  "price": 990.50,
                                  "stockQuantity": 0,
                                  "status": "OUT_OF_STOCK",
                                  "imageUrl": "https://example.com/cream.jpg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Крем для лица, 40 мл"))
                .andExpect(jsonPath("$.category").value("MAKEUP"))
                .andExpect(jsonPath("$.stockQuantity").value(0))
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));
    }

    @Test
    void updateOrder_newOrder_replacesItems() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productId").value(10))
                .andExpect(jsonPath("$.totalAmount").value(540.50));

        mockMvc.perform(get("/api/v1/products/6"))
                .andExpect(jsonPath("$.stockQuantity").value(2));
        mockMvc.perform(get("/api/v1/products/9"))
                .andExpect(jsonPath("$.stockQuantity").value(62));
        mockMvc.perform(get("/api/v1/products/10"))
                .andExpect(jsonPath("$.stockQuantity").value(26));

        mockMvc.perform(patch("/api/v1/orders/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_CANNOT_BE_EDITED"));

        mockMvc.perform(patch("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "items": [{"productId": 10, "quantity": 1}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_CANNOT_BE_EDITED"));
    }
}
