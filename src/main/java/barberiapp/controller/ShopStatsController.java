package barberiapp.controller;

import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/shops/{shopId}/stats")
@RequiredArgsConstructor
public class ShopStatsController {

    private final BarberShopRepository shopRepository;
    private final BarberShopMemberRepository memberRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentProductRepository appointmentProductRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getStats(
            @PathVariable String shopId,
            @RequestParam(defaultValue = "30") int days,
            Authentication auth) {

        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(auth.getName())) {
            return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado"));
        }

        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();

        // Barber IDs for legacy appointments query
        List<Long> barberIds = memberRepository.findByShopIdAndActiveTrue(shopId)
                .stream()
                .map(m -> m.getBarber().getId())
                .collect(Collectors.toList());

        List<Long> queryIds = barberIds.isEmpty() ? List.of(-1L) : barberIds;

        List<Appointment> allAppointments = appointmentRepository
                .findByShopOrLegacyBarbers(shopId, queryIds)
                .stream()
                .filter(a -> !a.getDate().isBefore(from) && !a.getDate().isAfter(to))
                .collect(Collectors.toList());

        List<Appointment> completed = allAppointments.stream()
                .filter(a -> "completed".equals(a.getStatus()))
                .collect(Collectors.toList());

        // ── Revenue ────────────────────────────────────────────────────────────
        long totalRevenue = completed.stream()
                .mapToLong(a -> a.getTotalPrice() != null ? a.getTotalPrice() : 0).sum();
        long cashRevenue = completed.stream()
                .filter(a -> "cash".equals(a.getPaymentMethod()))
                .mapToLong(a -> a.getTotalPrice() != null ? a.getTotalPrice() : 0).sum();
        long transferRevenue = completed.stream()
                .filter(a -> "transfer".equals(a.getPaymentMethod()))
                .mapToLong(a -> a.getTotalPrice() != null ? a.getTotalPrice() : 0).sum();
        long subscriptionRevenue = completed.stream()
                .filter(a -> a.getSubscriptionId() != null)
                .mapToLong(a -> a.getTotalPrice() != null ? a.getTotalPrice() : 0).sum();

        // ── Appointment counts ────────────────────────────────────────────────
        Map<String, Long> byStatus = allAppointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatus() != null ? a.getStatus() : "unknown",
                        Collectors.counting()));

        long atHome = allAppointments.stream()
                .filter(a -> "home".equals(a.getLocation())).count();
        long atShop = allAppointments.size() - atHome;

        // ── Weekly revenue trend ──────────────────────────────────────────────
        Map<String, long[]> weeklyMap = new TreeMap<>();
        for (Appointment a : completed) {
            String weekKey = a.getDate()
                    .with(WeekFields.ISO.dayOfWeek(), 1)
                    .toString();
            weeklyMap.computeIfAbsent(weekKey, k -> new long[]{0, 0});
            weeklyMap.get(weekKey)[0] += a.getTotalPrice() != null ? a.getTotalPrice() : 0;
            weeklyMap.get(weekKey)[1]++;
        }
        List<Map<String, Object>> weeklyRevenue = weeklyMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> w = new LinkedHashMap<>();
                    w.put("week", e.getKey());
                    w.put("revenue", e.getValue()[0]);
                    w.put("count", e.getValue()[1]);
                    return w;
                })
                .collect(Collectors.toList());

        // ── Top services ──────────────────────────────────────────────────────
        Map<String, long[]> serviceMap = new LinkedHashMap<>();
        for (Appointment a : completed) {
            if (a.getService() != null) {
                String name = a.getService().getName();
                serviceMap.computeIfAbsent(name, k -> new long[]{0, 0});
                serviceMap.get(name)[0]++;
                serviceMap.get(name)[1] += a.getTotalPrice() != null ? a.getTotalPrice() : 0;
            }
        }
        List<Map<String, Object>> topServices = serviceMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("name", e.getKey());
                    s.put("count", e.getValue()[0]);
                    s.put("revenue", e.getValue()[1]);
                    return s;
                })
                .collect(Collectors.toList());

        // ── Top products ──────────────────────────────────────────────────────
        Map<String, long[]> productMap = new LinkedHashMap<>();
        for (Appointment a : completed) {
            List<AppointmentProduct> products =
                    appointmentProductRepository.findByAppointmentId(a.getId());
            for (AppointmentProduct p : products) {
                String name = p.getProductName() != null ? p.getProductName() : "Desconocido";
                productMap.computeIfAbsent(name, k -> new long[]{0, 0});
                productMap.get(name)[0] += p.getQuantity() != null ? p.getQuantity() : 1;
                productMap.get(name)[1] += p.getSubtotal() != null ? p.getSubtotal() : 0;
            }
        }
        List<Map<String, Object>> topProducts = productMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("name", e.getKey());
                    s.put("quantity", e.getValue()[0]);
                    s.put("revenue", e.getValue()[1]);
                    return s;
                })
                .collect(Collectors.toList());

        // ── Top clients ───────────────────────────────────────────────────────
        Map<String, long[]> clientMap = new LinkedHashMap<>();
        for (Appointment a : completed) {
            String name;
            if (a.getClientName() != null && !a.getClientName().isBlank()) {
                name = a.getClientName();
            } else if (a.getUser() != null && a.getUser().getFullName() != null) {
                name = a.getUser().getFullName();
            } else {
                name = "Anónimo";
            }
            clientMap.computeIfAbsent(name, k -> new long[]{0, 0});
            clientMap.get(name)[0]++;
            clientMap.get(name)[1] += a.getTotalPrice() != null ? a.getTotalPrice() : 0;
        }
        List<Map<String, Object>> topClients = clientMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> {
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("name", e.getKey());
                    s.put("visits", e.getValue()[0]);
                    s.put("totalSpent", e.getValue()[1]);
                    return s;
                })
                .collect(Collectors.toList());

        // ── Barber performance ────────────────────────────────────────────────
        // Use Object[] array: [name, count, revenue, rating]
        Map<Long, Object[]> barberPerfMap = new LinkedHashMap<>();
        for (Appointment a : allAppointments) {
            if (a.getBarber() == null) continue;
            Long bid = a.getBarber().getId();
            barberPerfMap.computeIfAbsent(bid, k -> new Object[]{
                    a.getBarber().getName(), 0L, 0L, a.getBarber().getRating()
            });
            if ("completed".equals(a.getStatus())) {
                barberPerfMap.get(bid)[1] = (long) barberPerfMap.get(bid)[1] + 1;
                barberPerfMap.get(bid)[2] = (long) barberPerfMap.get(bid)[2]
                        + (a.getTotalPrice() != null ? a.getTotalPrice() : 0);
            }
        }
        List<Map<String, Object>> barberPerformance = barberPerfMap.values().stream()
                .sorted((a, b) -> Long.compare((long) b[1], (long) a[1]))
                .map(arr -> {
                    Map<String, Object> b = new LinkedHashMap<>();
                    b.put("name", arr[0]);
                    b.put("appointments", arr[1]);
                    b.put("revenue", arr[2]);
                    b.put("rating", arr[3]);
                    return b;
                })
                .collect(Collectors.toList());

        // ── Subscriptions ─────────────────────────────────────────────────────
        List<UserSubscription> allSubs =
                subscriptionRepository.findByShopIdOrderByCreatedAtDesc(shopId);
        long activeSubsCount = allSubs.stream()
                .filter(s -> "active".equals(s.getStatus())).count();
        long subsRevenueTotal = allSubs.stream()
                .mapToLong(s -> s.getPriceCharged() != null ? s.getPriceCharged() : 0).sum();
        long newSubsThisPeriod = allSubs.stream()
                .filter(s -> s.getCreatedAt() != null
                        && !s.getCreatedAt().toLocalDate().isBefore(from))
                .count();

        // ── Build response ────────────────────────────────────────────────────
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("period", Map.of("from", from.toString(), "to", to.toString(), "days", days));
        response.put("revenue", Map.of(
                "total", totalRevenue,
                "byCash", cashRevenue,
                "byTransfer", transferRevenue,
                "bySubscription", subscriptionRevenue
        ));
        response.put("appointments", Map.of(
                "total", (long) allAppointments.size(),
                "completed", byStatus.getOrDefault("completed", 0L),
                "cancelled", byStatus.getOrDefault("cancelled", 0L),
                "noShow", byStatus.getOrDefault("no_show", 0L),
                "pending", byStatus.getOrDefault("pending", 0L),
                "confirmed", byStatus.getOrDefault("confirmed", 0L),
                "atShop", atShop,
                "atHome", atHome
        ));
        response.put("weeklyRevenue", weeklyRevenue);
        response.put("topServices", topServices);
        response.put("topProducts", topProducts);
        response.put("topClients", topClients);
        response.put("barberPerformance", barberPerformance);
        response.put("subscriptions", Map.of(
                "active", activeSubsCount,
                "totalRevenue", subsRevenueTotal,
                "newThisPeriod", newSubsThisPeriod
        ));

        return ResponseEntity.ok(response);
    }
}
