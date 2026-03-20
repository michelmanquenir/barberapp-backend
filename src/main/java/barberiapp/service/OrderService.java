package barberiapp.service;

import barberiapp.dto.*;
import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final BarberShopRepository barberShopRepository;
    private final ProfileRepository profileRepository;

    // ── Estados terminales donde no se puede cambiar nada ────────────────────
    private static final Set<String> TERMINAL_STATUSES = Set.of("delivered", "cancelled");

    // ── Crear pedido ──────────────────────────────────────────────────────────

    @Transactional
    public OrderResponse createOrder(String clientUserId, OrderRequest request) {
        // Validar items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        // Validar negocio
        BarberShop shop = barberShopRepository.findById(request.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        // Validar dirección si es delivery
        if ("delivery".equals(request.getDeliveryType())
                && (request.getClientAddress() == null || request.getClientAddress().isBlank())) {
            throw new IllegalArgumentException("Se requiere dirección para delivery");
        }

        // Resolver y validar cada producto — fail fast en primer error
        List<Product> resolvedProducts = request.getItems().stream().map(item -> {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
            }
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado: ID " + item.getProductId()));
            if (!Boolean.TRUE.equals(p.getActive())) {
                throw new IllegalArgumentException("El producto \"" + p.getName() + "\" no está disponible");
            }
            if (p.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para \"" + p.getName() + "\". " +
                        "Disponible: " + p.getStock() + ", solicitado: " + item.getQuantity());
            }
            return p;
        }).collect(Collectors.toList());

        // Calcular total
        int totalPrice = 0;
        for (int i = 0; i < request.getItems().size(); i++) {
            totalPrice += resolvedProducts.get(i).getSalePrice() * request.getItems().get(i).getQuantity();
        }

        // Snapshot del nombre del cliente
        String clientName = profileRepository.findById(clientUserId)
                .map(Profile::getFullName)
                .orElse("Cliente");

        // Guardar pedido
        ShopOrder order = ShopOrder.builder()
                .shopId(request.getShopId())
                .clientUserId(clientUserId)
                .clientName(clientName)
                .status("pending")
                .deliveryType(request.getDeliveryType() != null ? request.getDeliveryType() : "pickup")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "cash")
                .clientAddress(request.getClientAddress())
                .totalPrice(totalPrice)
                .notes(request.getNotes())
                .build();

        ShopOrder savedOrder = shopOrderRepository.save(order);

        // Descontar stock y guardar ítems
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemReq = request.getItems().get(i);
            Product product = resolvedProducts.get(i);

            // Descontar stock
            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            // Guardar snapshot del ítem
            OrderItem orderItem = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getSalePrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(product.getSalePrice() * itemReq.getQuantity())
                    .build();
            orderItemRepository.save(orderItem);
        }

        return buildOrderResponse(savedOrder, shop.getName());
    }

    // ── Pedidos del cliente ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderResponse> getClientOrders(String clientUserId) {
        return shopOrderRepository
                .findByClientUserIdOrderByCreatedAtDesc(clientUserId)
                .stream()
                .map(o -> {
                    String shopName = barberShopRepository.findById(o.getShopId())
                            .map(BarberShop::getName).orElse("Negocio");
                    return buildOrderResponse(o, shopName);
                })
                .collect(Collectors.toList());
    }

    // ── Pedidos del negocio (admin) ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<OrderResponse> getShopOrders(String shopId, String requesterId) {
        BarberShop shop = barberShopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("Acceso denegado");
        }
        return shopOrderRepository
                .findByShopIdOrderByCreatedAtDesc(shopId)
                .stream()
                .map(o -> buildOrderResponse(o, shop.getName()))
                .collect(Collectors.toList());
    }

    // ── Actualizar estado (admin) ─────────────────────────────────────────────

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String requesterId, String newStatus) {
        ShopOrder order = shopOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        BarberShop shop = barberShopRepository.findById(order.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("Acceso denegado");
        }

        String previousStatus = order.getStatus();
        if (TERMINAL_STATUSES.contains(previousStatus)) {
            throw new IllegalArgumentException("El pedido ya está en estado final: " + previousStatus);
        }

        // Restaurar stock si se cancela
        if ("cancelled".equals(newStatus) && !"cancelled".equals(previousStatus)) {
            restoreStock(orderId);
        }

        order.setStatus(newStatus);
        ShopOrder saved = shopOrderRepository.save(order);
        return buildOrderResponse(saved, shop.getName());
    }

    // ── Cancelar pedido (cliente, solo desde pending) ─────────────────────────

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String clientUserId) {
        ShopOrder order = shopOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if (!order.getClientUserId().equals(clientUserId)) {
            throw new IllegalArgumentException("Acceso denegado");
        }
        if (!"pending".equals(order.getStatus())) {
            throw new IllegalArgumentException(
                    "Solo puedes cancelar pedidos pendientes. Este pedido está: " + order.getStatus());
        }

        restoreStock(orderId);
        order.setStatus("cancelled");
        ShopOrder saved = shopOrderRepository.save(order);

        String shopName = barberShopRepository.findById(order.getShopId())
                .map(BarberShop::getName).orElse("Negocio");
        return buildOrderResponse(saved, shopName);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            if (item.getProductId() == null) continue;
            productRepository.findById(item.getProductId()).ifPresentOrElse(
                    product -> {
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepository.save(product);
                    },
                    () -> log.warn("Producto {} ya no existe, no se restauró stock del item {}", item.getProductId(), item.getId())
            );
        }
    }

    private OrderResponse buildOrderResponse(ShopOrder order, String shopName) {
        List<OrderItemResponse> items = orderItemRepository
                .findByOrderId(order.getId())
                .stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .shopId(order.getShopId())
                .shopName(shopName)
                .clientUserId(order.getClientUserId())
                .clientName(order.getClientName())
                .status(order.getStatus())
                .deliveryType(order.getDeliveryType())
                .paymentMethod(order.getPaymentMethod())
                .clientAddress(order.getClientAddress())
                .totalPrice(order.getTotalPrice())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
