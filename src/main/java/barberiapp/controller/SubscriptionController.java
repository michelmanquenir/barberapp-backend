package barberiapp.controller;

import barberiapp.dto.SubscriptionPlanRequest;
import barberiapp.dto.SubscriptionPlanResponse;
import barberiapp.dto.UserSubscriptionResponse;
import barberiapp.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ══════════════════════════════════════════════════════════
    // PLANES — Públicos (cualquier visitante puede listarlos)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/shops/{shopId}/subscription-plans
     * Lista los planes activos de una barbería (acceso público).
     */
    @GetMapping("/api/shops/{shopId}/subscription-plans")
    public List<SubscriptionPlanResponse> getPublicPlans(@PathVariable String shopId) {
        return subscriptionService.getPublicPlans(shopId);
    }

    // ══════════════════════════════════════════════════════════
    // PLANES — Admin (dueño de la barbería)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/admin/shops/{shopId}/subscription-plans
     * Lista todos los planes (activos e inactivos) de una barbería.
     * Solo el dueño debería llamar a este endpoint.
     */
    @GetMapping("/api/admin/shops/{shopId}/subscription-plans")
    public List<SubscriptionPlanResponse> getAdminPlans(@PathVariable String shopId) {
        return subscriptionService.getAdminPlans(shopId);
    }

    /**
     * POST /api/admin/shops/{shopId}/subscription-plans
     * Crea un nuevo plan.
     */
    @PostMapping("/api/admin/shops/{shopId}/subscription-plans")
    public ResponseEntity<SubscriptionPlanResponse> createPlan(
            @PathVariable String shopId,
            @RequestBody SubscriptionPlanRequest req) {
        return ResponseEntity.ok(subscriptionService.createPlan(shopId, req));
    }

    /**
     * PUT /api/admin/subscription-plans/{planId}
     * Actualiza un plan existente.
     */
    @PutMapping("/api/admin/subscription-plans/{planId}")
    public ResponseEntity<SubscriptionPlanResponse> updatePlan(
            @PathVariable Long planId,
            @RequestBody SubscriptionPlanRequest req) {
        return ResponseEntity.ok(subscriptionService.updatePlan(planId, req));
    }

    /**
     * DELETE /api/admin/subscription-plans/{planId}
     * Desactiva un plan (soft delete).
     */
    @DeleteMapping("/api/admin/subscription-plans/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        subscriptionService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/shops/{shopId}/subscribers
     * Lista de suscriptores activos de la barbería.
     */
    @GetMapping("/api/admin/shops/{shopId}/subscribers")
    public List<UserSubscriptionResponse> getSubscribers(@PathVariable String shopId) {
        return subscriptionService.getShopSubscribers(shopId);
    }

    /**
     * GET /api/admin/shops/{shopId}/subscriptions/history
     * Historial completo de suscripciones de la barbería.
     */
    @GetMapping("/api/admin/shops/{shopId}/subscriptions/history")
    public List<UserSubscriptionResponse> getSubscriptionHistory(@PathVariable String shopId) {
        return subscriptionService.getShopSubscriptionHistory(shopId);
    }

    // ══════════════════════════════════════════════════════════
    // SUSCRIPCIONES — Cliente autenticado
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/subscriptions/subscribe/{planId}
     * Suscribe al usuario autenticado al plan indicado.
     * Descuenta del wallet.
     */
    @PostMapping("/api/subscriptions/subscribe/{planId}")
    public ResponseEntity<UserSubscriptionResponse> subscribe(@PathVariable Long planId) {
        String userId = currentUserId();
        return ResponseEntity.ok(subscriptionService.subscribe(userId, planId));
    }

    /**
     * GET /api/subscriptions/me
     * Todas las suscripciones del usuario autenticado.
     */
    @GetMapping("/api/subscriptions/me")
    public List<UserSubscriptionResponse> getMySubscriptions() {
        return subscriptionService.getMySubscriptions(currentUserId());
    }

    /**
     * GET /api/subscriptions/me/active?shopId={shopId}
     * Suscripción activa del usuario en una barbería específica.
     */
    @GetMapping("/api/subscriptions/me/active")
    public ResponseEntity<UserSubscriptionResponse> getMyActiveSubscription(
            @RequestParam String shopId) {
        return subscriptionService.getMyActiveSubscription(currentUserId(), shopId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── helper ──────────────────────────────────────────────────────────────────

    private String currentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
