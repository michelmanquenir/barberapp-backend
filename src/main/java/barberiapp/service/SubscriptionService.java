package barberiapp.service;

import barberiapp.dto.SubscriptionPlanRequest;
import barberiapp.dto.SubscriptionPlanResponse;
import barberiapp.dto.UserSubscriptionResponse;
import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final BarberShopRepository shopRepository;
    private final ProfileRepository profileRepository;
    private final TransactionRepository transactionRepository;
    private final WalletService walletService;

    // ══════════════════════════════════════════════════════════
    // PLANES (ADMIN — dueño de la barbería)
    // ══════════════════════════════════════════════════════════

    /** Lista todos los planes de una barbería (incluyendo inactivos — para el admin) */
    public List<SubscriptionPlanResponse> getAdminPlans(String shopId) {
        return planRepository.findByShopIdOrderByPriceAsc(shopId)
                .stream().map(SubscriptionPlanResponse::from).toList();
    }

    /** Lista los planes activos de una barbería (para clientes) */
    public List<SubscriptionPlanResponse> getPublicPlans(String shopId) {
        return planRepository.findByShopIdAndActiveTrueOrderByPriceAsc(shopId)
                .stream().map(SubscriptionPlanResponse::from).toList();
    }

    /** Crea un nuevo plan */
    @Transactional
    public SubscriptionPlanResponse createPlan(String shopId, SubscriptionPlanRequest req) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Barbería no encontrada"));

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setShopId(shopId);
        plan.setShopName(shop.getName());
        plan.setName(req.getName());
        plan.setDescription(req.getDescription());
        plan.setPrice(req.getPrice());
        plan.setCutsPerPeriod(req.getCutsPerPeriod());
        plan.setActive(req.getActive() != null ? req.getActive() : true);
        return SubscriptionPlanResponse.from(planRepository.save(plan));
    }

    /** Actualiza un plan existente */
    @Transactional
    public SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest req) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        if (req.getName() != null)         plan.setName(req.getName());
        if (req.getDescription() != null)  plan.setDescription(req.getDescription());
        if (req.getPrice() != null)        plan.setPrice(req.getPrice());
        if (req.getCutsPerPeriod() != null) plan.setCutsPerPeriod(req.getCutsPerPeriod());
        if (req.getActive() != null)       plan.setActive(req.getActive());
        return SubscriptionPlanResponse.from(planRepository.save(plan));
    }

    /** Elimina un plan (solo si no tiene suscripciones activas) */
    @Transactional
    public void deletePlan(Long planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
        // Desactivar en lugar de borrar físicamente para preservar el historial
        plan.setActive(false);
        planRepository.save(plan);
    }

    // ══════════════════════════════════════════════════════════
    // SUSCRIPCIONES (CLIENTE)
    // ══════════════════════════════════════════════════════════

    /** Suscribe a un cliente a un plan. Descuenta del wallet. */
    @Transactional
    public UserSubscriptionResponse subscribe(String userId, Long planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));

        if (!plan.getActive()) {
            throw new IllegalStateException("Este plan ya no está disponible");
        }

        // Verificar que no tenga ya una suscripción activa en esa barbería
        Optional<UserSubscription> existing = subscriptionRepository.findActiveByUserAndShop(
                userId, plan.getShopId(), LocalDate.now());
        if (existing.isPresent()) {
            throw new IllegalStateException("Ya tienes una suscripción activa en esta barbería");
        }

        // Verificar balance de wallet
        int balance = walletService.getUserBalance(userId);
        if (balance < plan.getPrice()) {
            throw new IllegalStateException("Saldo insuficiente. Necesitas $" + plan.getPrice()
                    + " y tienes $" + balance);
        }

        // Obtener perfil del usuario
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Cobrar del wallet
        Transaction debit = new Transaction();
        debit.setUser(profile);
        debit.setType("debit");
        debit.setAmount(plan.getPrice());
        debit.setDescription("Suscripción mensual — " + plan.getName() + " en " + plan.getShopName());
        transactionRepository.save(debit);

        // Crear suscripción
        UserSubscription sub = new UserSubscription();
        sub.setUserId(userId);
        sub.setUserName(profile.getFullName());
        sub.setShopId(plan.getShopId());
        sub.setShopName(plan.getShopName());
        sub.setPlan(plan);
        sub.setPlanName(plan.getName());
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusDays(30));
        sub.setCutsUsed(0);
        sub.setCutsAllowed(plan.getCutsPerPeriod());
        sub.setPriceCharged(plan.getPrice());
        sub.setStatus("active");

        return UserSubscriptionResponse.from(subscriptionRepository.save(sub));
    }

    /** Mis suscripciones (todas) */
    public List<UserSubscriptionResponse> getMySubscriptions(String userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(UserSubscriptionResponse::from).toList();
    }

    /** Mi suscripción activa en una barbería específica */
    public Optional<UserSubscriptionResponse> getMyActiveSubscription(String userId, String shopId) {
        return subscriptionRepository.findActiveByUserAndShop(userId, shopId, LocalDate.now())
                .map(UserSubscriptionResponse::from);
    }

    /** Lista de suscriptores activos de una barbería (admin) */
    public List<UserSubscriptionResponse> getShopSubscribers(String shopId) {
        return subscriptionRepository.findActiveByShop(shopId, LocalDate.now())
                .stream().map(UserSubscriptionResponse::from).toList();
    }

    /** Historial de suscripciones de una barbería (admin) */
    public List<UserSubscriptionResponse> getShopSubscriptionHistory(String shopId) {
        return subscriptionRepository.findByShopIdOrderByCreatedAtDesc(shopId)
                .stream().map(UserSubscriptionResponse::from).toList();
    }

    // ══════════════════════════════════════════════════════════
    // USO DE CORTES (llamado desde AppointmentService)
    // ══════════════════════════════════════════════════════════

    /**
     * Descuenta un corte de la suscripción activa del usuario en la barbería dada.
     * @return La suscripción actualizada
     */
    @Transactional
    public UserSubscription consumeCut(String userId, String shopId) {
        UserSubscription sub = subscriptionRepository
                .findActiveByUserAndShop(userId, shopId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException(
                        "No tienes una suscripción activa en esta barbería"));

        if (!sub.isUsable()) {
            throw new IllegalStateException(
                    "Tu suscripción no tiene cortes disponibles o está vencida");
        }

        sub.setCutsUsed(sub.getCutsUsed() + 1);
        return subscriptionRepository.save(sub);
    }

    /**
     * Devuelve un corte a la suscripción (cuando se cancela la cita).
     */
    @Transactional
    public void refundCut(String userId, String shopId) {
        subscriptionRepository.findActiveByUserAndShop(userId, shopId, LocalDate.now())
                .ifPresent(sub -> {
                    if (sub.getCutsUsed() > 0) {
                        sub.setCutsUsed(sub.getCutsUsed() - 1);
                        subscriptionRepository.save(sub);
                    }
                });
    }
}
