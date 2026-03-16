package barberiapp.controller;

import barberiapp.dto.UserSummary;
import barberiapp.model.*;
import barberiapp.repository.AppUserRepository;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.BusinessCategoryRepository;
import barberiapp.repository.ProductRepository;
import barberiapp.repository.ProfileRepository;
import barberiapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final BarberShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final BusinessCategoryRepository categoryRepository;
    private final EmailService emailService;

    // ─── Usuarios ────────────────────────────────────────────────────────────────

    @GetMapping("/users")
    public List<UserSummary> listUsers() {
        return appUserRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.SUPER_ADMIN)
                .map(this::toUserSummary)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/status/{status}")
    public List<UserSummary> listUsersByStatus(@PathVariable String status) {
        UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
        return appUserRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.SUPER_ADMIN && u.getStatus() == userStatus)
                .map(this::toUserSummary)
                .collect(Collectors.toList());
    }

    @PutMapping("/users/{userId}/approve")
    public ResponseEntity<Map<String, String>> approveUser(@PathVariable String userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setStatus(UserStatus.ACTIVE);
        appUserRepository.save(user);
        String name = profileRepository.findById(userId).map(p -> p.getFullName()).orElse(user.getEmail());
        emailService.sendAccountApproved(user.getEmail(), name);
        return ResponseEntity.ok(Map.of("status", "ACTIVE", "userId", userId));
    }

    @PutMapping("/users/{userId}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(@PathVariable String userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setStatus(UserStatus.REJECTED);
        appUserRepository.save(user);
        String name = profileRepository.findById(userId).map(p -> p.getFullName()).orElse(user.getEmail());
        emailService.sendAccountRejected(user.getEmail(), name);
        return ResponseEntity.ok(Map.of("status", "REJECTED", "userId", userId));
    }

    // ─── Negocios ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @GetMapping("/shops")
    public List<Map<String, Object>> listShops() {
        return shopRepository.findAll().stream()
                .map(this::toShopSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @GetMapping("/shops/status/{status}")
    public List<Map<String, Object>> listShopsByStatus(@PathVariable String status) {
        ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
        return shopRepository.findAll().stream()
                .filter(s -> {
                    ApprovalStatus cur = s.getApprovalStatus() != null ? s.getApprovalStatus() : ApprovalStatus.ACTIVE;
                    return cur == approvalStatus;
                })
                .map(this::toShopSummary)
                .collect(Collectors.toList());
    }

    @Transactional
    @PutMapping("/shops/{shopId}/approve")
    public ResponseEntity<Map<String, String>> approveShop(@PathVariable String shopId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        shop.setApprovalStatus(ApprovalStatus.ACTIVE);
        shopRepository.save(shop);
        AppUser owner = shop.getOwner();
        String ownerName = profileRepository.findById(owner.getId()).map(p -> p.getFullName()).orElse(owner.getEmail());
        emailService.sendShopApproved(owner.getEmail(), ownerName, shop.getName());
        return ResponseEntity.ok(Map.of("approvalStatus", "ACTIVE", "shopId", shopId));
    }

    @Transactional
    @PutMapping("/shops/{shopId}/reject")
    public ResponseEntity<Map<String, String>> rejectShop(@PathVariable String shopId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        shop.setApprovalStatus(ApprovalStatus.REJECTED);
        shopRepository.save(shop);
        AppUser owner = shop.getOwner();
        String ownerName = profileRepository.findById(owner.getId()).map(p -> p.getFullName()).orElse(owner.getEmail());
        emailService.sendShopRejected(owner.getEmail(), ownerName, shop.getName());
        return ResponseEntity.ok(Map.of("approvalStatus", "REJECTED", "shopId", shopId));
    }

    // ─── Productos ────────────────────────────────────────────────────────────

    @GetMapping("/products")
    public List<Map<String, Object>> listProducts() {
        return productRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::toProductSummary)
                .collect(Collectors.toList());
    }

    @GetMapping("/products/status/{status}")
    public List<Map<String, Object>> listProductsByStatus(@PathVariable String status) {
        ApprovalStatus approvalStatus = ApprovalStatus.valueOf(status.toUpperCase());
        return productRepository.findByApprovalStatus(approvalStatus).stream()
                .map(this::toProductSummary)
                .collect(Collectors.toList());
    }

    @PutMapping("/products/{productId}/approve")
    public ResponseEntity<Map<String, String>> approveProduct(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        product.setApprovalStatus(ApprovalStatus.ACTIVE);
        productRepository.save(product);
        notifyProductOwner(product, true);
        return ResponseEntity.ok(Map.of("approvalStatus", "ACTIVE", "productId", productId.toString()));
    }

    @PutMapping("/products/{productId}/reject")
    public ResponseEntity<Map<String, String>> rejectProduct(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        product.setApprovalStatus(ApprovalStatus.REJECTED);
        productRepository.save(product);
        notifyProductOwner(product, false);
        return ResponseEntity.ok(Map.of("approvalStatus", "REJECTED", "productId", productId.toString()));
    }

    private void notifyProductOwner(Product product, boolean approved) {
        shopRepository.findById(product.getShopId()).ifPresent(shop -> {
            AppUser owner = shop.getOwner();
            String ownerName = profileRepository.findById(owner.getId()).map(p -> p.getFullName()).orElse(owner.getEmail());
            if (approved) {
                emailService.sendProductApproved(owner.getEmail(), ownerName, product.getName());
            } else {
                emailService.sendProductRejected(owner.getEmail(), ownerName, product.getName());
            }
        });
    }

    // ─── Stats globales ──────────────────────────────────────────────────────

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        long pendingUsers    = appUserRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.SUPER_ADMIN && u.getStatus() == UserStatus.PENDING).count();
        long pendingShops    = shopRepository.findAll().stream()
                .filter(s -> s.getApprovalStatus() == ApprovalStatus.PENDING).count();
        long pendingProducts = productRepository.findByApprovalStatus(ApprovalStatus.PENDING).size();
        long totalUsers      = appUserRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.SUPER_ADMIN).count();
        long totalShops      = shopRepository.count();
        long totalProducts   = productRepository.count();

        return Map.of(
                "pendingUsers",    pendingUsers,
                "pendingShops",    pendingShops,
                "pendingProducts", pendingProducts,
                "totalUsers",      totalUsers,
                "totalShops",      totalShops,
                "totalProducts",   totalProducts
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private UserSummary toUserSummary(AppUser user) {
        Profile profile = profileRepository.findById(user.getId()).orElse(null);
        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus() != null ? user.getStatus().name() : UserStatus.PENDING.name(),
                profile != null ? profile.getFullName() : null,
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getDniUrl() : null,
                user.getCreatedAt()
        );
    }

    private Map<String, Object> toShopSummary(BarberShop shop) {
        ApprovalStatus status = shop.getApprovalStatus() != null ? shop.getApprovalStatus() : ApprovalStatus.ACTIVE;
        Profile ownerProfile  = profileRepository.findById(shop.getOwner().getId()).orElse(null);
        String ownerName      = (ownerProfile != null && ownerProfile.getFullName() != null)
                ? ownerProfile.getFullName() : shop.getOwner().getEmail();
        return Map.of(
                "id",             shop.getId(),
                "name",           shop.getName(),
                "description",    shop.getDescription() != null ? shop.getDescription() : "",
                "slug",           shop.getSlug(),
                "address",        shop.getAddress()    != null ? shop.getAddress() : "",
                "active",         Boolean.TRUE.equals(shop.getActive()),
                "approvalStatus", status.name(),
                "ownerEmail",     shop.getOwner().getEmail(),
                "ownerName",      ownerName,
                "createdAt",      shop.getCreatedAt() != null ? shop.getCreatedAt().toString() : ""
        );
    }

    private Map<String, Object> toProductSummary(Product product) {
        ApprovalStatus status = product.getApprovalStatus() != null ? product.getApprovalStatus() : ApprovalStatus.ACTIVE;
        String shopName = shopRepository.findById(product.getShopId())
                .map(BarberShop::getName).orElse(product.getShopId());
        return Map.ofEntries(
                Map.entry("id",             (Object) product.getId()),
                Map.entry("name",           product.getName()),
                Map.entry("description",    product.getDescription() != null ? product.getDescription() : ""),
                Map.entry("category",       product.getCategory()    != null ? product.getCategory()    : ""),
                Map.entry("imageUrl",       product.getImageUrl()    != null ? product.getImageUrl()    : ""),
                Map.entry("salePrice",      (Object)(product.getSalePrice()  != null ? product.getSalePrice()  : 0)),
                Map.entry("stock",          (Object)(product.getStock()      != null ? product.getStock()      : 0)),
                Map.entry("approvalStatus", status.name()),
                Map.entry("shopName",       shopName),
                Map.entry("shopId",         product.getShopId()),
                Map.entry("createdAt",      product.getCreatedAt() != null ? product.getCreatedAt().toString() : "")
        );
    }

    // ─── Categorías de negocio ────────────────────────────────────────────────

    @GetMapping("/categories")
    public List<Map<String, Object>> listCategories() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(c -> c.getSortOrder() != null ? c.getSortOrder() : 0))
                .map(this::toCategorySummary)
                .collect(Collectors.toList());
    }

    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String slug = (String) body.getOrDefault("slug", "");
        String icon = (String) body.getOrDefault("icon", "");
        String description = (String) body.getOrDefault("description", "");
        int sortOrder = body.containsKey("sortOrder") ? ((Number) body.get("sortOrder")).intValue() : 0;

        if (slug.isBlank()) throw new IllegalArgumentException("El slug es requerido");
        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("El slug '" + slug + "' ya está en uso");
        }

        BusinessCategory category = BusinessCategory.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .slug(slug)
                .icon(icon)
                .description(description)
                .sortOrder(sortOrder)
                .active(true)
                .build();

        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCategorySummary(category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        BusinessCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        if (body.containsKey("name"))        category.setName((String) body.get("name"));
        if (body.containsKey("icon"))        category.setIcon((String) body.get("icon"));
        if (body.containsKey("description")) category.setDescription((String) body.get("description"));
        if (body.containsKey("sortOrder"))   category.setSortOrder(((Number) body.get("sortOrder")).intValue());
        if (body.containsKey("active"))      category.setActive((Boolean) body.get("active"));
        if (body.containsKey("slug")) {
            String newSlug = (String) body.get("slug");
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("El slug '" + newSlug + "' ya está en uso");
            }
            category.setSlug(newSlug);
        }

        categoryRepository.save(category);
        return ResponseEntity.ok(toCategorySummary(category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable String id) {
        BusinessCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        category.setActive(false);
        categoryRepository.save(category);
        return ResponseEntity.ok(Map.of("status", "deactivated", "id", id));
    }

    private Map<String, Object> toCategorySummary(BusinessCategory c) {
        return Map.of(
                "id",          c.getId(),
                "name",        c.getName(),
                "slug",        c.getSlug(),
                "icon",        c.getIcon()        != null ? c.getIcon()        : "",
                "description", c.getDescription() != null ? c.getDescription() : "",
                "active",      Boolean.TRUE.equals(c.getActive()),
                "sortOrder",   c.getSortOrder()   != null ? c.getSortOrder()   : 0
        );
    }
}
