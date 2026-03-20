package barberiapp.controller;

import barberiapp.dto.OrderRequest;
import barberiapp.dto.OrderResponse;
import barberiapp.dto.UpdateOrderStatusRequest;
import barberiapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── Cliente ───────────────────────────────────────────────────────────────

    /** POST /api/orders — cliente crea un pedido */
    @PostMapping("/api/orders")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            String clientUserId = getCurrentUserId();
            OrderResponse order = orderService.createOrder(clientUserId, request);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/orders/my — cliente ve sus pedidos */
    @GetMapping("/api/orders/my")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        String clientUserId = getCurrentUserId();
        return ResponseEntity.ok(orderService.getClientOrders(clientUserId));
    }

    /** PUT /api/orders/{id}/cancel — cliente cancela pedido pendiente */
    @PutMapping("/api/orders/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            String clientUserId = getCurrentUserId();
            return ResponseEntity.ok(orderService.cancelOrder(id, clientUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    /** GET /api/admin/shops/{shopId}/orders — dueño ve pedidos de su negocio */
    @GetMapping("/api/admin/shops/{shopId}/orders")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getShopOrders(@PathVariable String shopId) {
        try {
            String requesterId = getCurrentUserId();
            return ResponseEntity.ok(orderService.getShopOrders(shopId, requesterId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/admin/orders/{id}/status — dueño actualiza estado del pedido */
    @PutMapping("/api/admin/orders/{id}/status")
    @Transactional
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                               @RequestBody UpdateOrderStatusRequest request) {
        try {
            String requesterId = getCurrentUserId();
            return ResponseEntity.ok(orderService.updateOrderStatus(id, requesterId, request.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
