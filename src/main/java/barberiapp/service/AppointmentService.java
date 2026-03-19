package barberiapp.service;

import barberiapp.dto.AppointmentProductItem;
import barberiapp.dto.BarberBookingRequest;
import barberiapp.model.Appointment;
import barberiapp.model.AppointmentProduct;
import barberiapp.model.AppUser;
import barberiapp.model.Barber;
import barberiapp.model.BarberShop;
import barberiapp.model.Product;
import barberiapp.model.Profile;
import barberiapp.model.ServiceEntity;
import barberiapp.model.UserStatus;
import barberiapp.model.UserSubscription;
import barberiapp.repository.AppointmentProductRepository;
import barberiapp.repository.AppointmentRepository;
import barberiapp.repository.AppUserRepository;
import barberiapp.repository.BarberRepository;
import barberiapp.repository.BarberShopMemberRepository;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.ProductRepository;
import barberiapp.repository.ProfileRepository;
import barberiapp.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentProductRepository appointmentProductRepository;
    private final AppUserRepository appUserRepository;
    private final ProfileRepository profileRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final BarberShopMemberRepository memberRepository;
    private final BarberShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;

    public List<Appointment> getUserAppointments(String userId) {
        return appointmentRepository.findByUserIdOrderByDateDescTimeDesc(userId);
    }

    @Transactional
    public Appointment createAppointment(Appointment appointmentReq, String userId, Long barberId, Long serviceId,
                                         boolean useSubscription, List<AppointmentProductItem> productItems) {
        // Verificar que el usuario está activo (aprobado por super admin)
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (appUser.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Tu cuenta aún no ha sido verificada. Un administrador debe aprobarla antes de poder agendar citas.");
        }

        // Validar que la cita sea con al menos 15 minutos de anticipación
        LocalDateTime appointmentDT = LocalDateTime.of(appointmentReq.getDate(), appointmentReq.getTime());
        if (appointmentDT.isBefore(LocalDateTime.now().plusMinutes(15))) {
            throw new IllegalArgumentException("La cita debe agendarse con al menos 15 minutos de anticipación");
        }

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        appointmentReq.setUser(profile);
        appointmentReq.setBarber(barber);
        appointmentReq.setService(service);

        // Duración: 180 min para domicilio, 30 min para normal
        boolean isHome = "home".equals(appointmentReq.getLocation());
        if (appointmentReq.getDurationMinutes() == null || appointmentReq.getDurationMinutes() == 0) {
            appointmentReq.setDurationMinutes(isHome ? 180 : 30);
        }

        // ── Validar y calcular subtotal de productos ────────────────────────────
        List<Product> resolvedProducts = new ArrayList<>();
        int productsSubtotal = 0;

        if (productItems != null && !productItems.isEmpty()) {
            for (AppointmentProductItem item : productItems) {
                if (item.getQuantity() == null || item.getQuantity() <= 0) continue;

                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Producto no encontrado: ID " + item.getProductId()));

                if (!Boolean.TRUE.equals(product.getActive())) {
                    throw new IllegalArgumentException("El producto \"" + product.getName() + "\" no está disponible");
                }
                if (product.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente para \"" + product.getName() + "\". " +
                            "Disponible: " + product.getStock() + ", solicitado: " + item.getQuantity());
                }

                productsSubtotal += product.getSalePrice() * item.getQuantity();
                resolvedProducts.add(product); // se usa luego para descontar stock
            }
        }

        // Precio total = servicio + recargo de domicilio + productos
        int surcharge = appointmentReq.getSurchargeAmount() != null ? appointmentReq.getSurchargeAmount() : 0;
        appointmentReq.setTotalPrice(service.getPrice() + surcharge + productsSubtotal);

        // Si el cliente paga con suscripción, consumir un corte
        if (useSubscription) {
            String shopId = appointmentReq.getShopId();
            if (shopId == null || shopId.isBlank()) {
                throw new IllegalArgumentException("Se requiere shopId para pagar con suscripción");
            }
            UserSubscription sub = subscriptionService.consumeCut(userId, shopId);
            appointmentReq.setPaymentMethod("subscription");
            appointmentReq.setSubscriptionId(sub.getId());
        }

        Appointment saved = appointmentRepository.save(appointmentReq);

        // ── Emails de notificación ──────────────────────────────────────────────
        try {
            EmailService.AppointmentEmailData emailData = buildEmailData(saved);
            // Al cliente
            String clientEmail = appUserRepository.findById(userId).map(AppUser::getEmail).orElse(null);
            String clientName  = profile.getFullName() != null ? profile.getFullName() : "Cliente";
            if (clientEmail != null) {
                emailService.sendAppointmentCreatedClient(clientEmail, clientName, emailData);
            }
            // Al propietario del negocio
            if (saved.getShopId() != null) {
                shopRepository.findById(saved.getShopId()).ifPresent(shop -> {
                    AppUser owner = shop.getOwner();
                    String ownerName = profileRepository.findById(owner.getId())
                            .map(p -> p.getFullName() != null ? p.getFullName() : owner.getEmail())
                            .orElse(owner.getEmail());
                    emailService.sendAppointmentCreatedOwner(owner.getEmail(), ownerName, clientName, emailData);
                });
            }
        } catch (Exception e) {
            // No fallar la cita por un error de email
        }

        // ── Persistir ítems de productos y descontar stock ──────────────────────
        if (productItems != null && !productItems.isEmpty()) {
            int idx = 0;
            for (AppointmentProductItem item : productItems) {
                if (item.getQuantity() == null || item.getQuantity() <= 0) continue;

                Product product = resolvedProducts.get(idx++);

                // Descontar stock
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);

                // Guardar snapshot del ítem en la cita
                AppointmentProduct ap = new AppointmentProduct();
                ap.setAppointmentId(saved.getId());
                ap.setProductId(product.getId());
                ap.setProductName(product.getName());
                ap.setUnitPrice(product.getSalePrice());
                ap.setQuantity(item.getQuantity());
                ap.setSubtotal(product.getSalePrice() * item.getQuantity());
                appointmentProductRepository.save(ap);
            }
        }

        return saved;
    }

    /**
     * Permite que un barbero agende una cita para un cliente (walk-in o registrado).
     * No aplica la restricción de 15 minutos ya que el barbero agenda directamente.
     */
    @Transactional
    public Appointment createBarberAppointment(BarberBookingRequest req, String requesterId) {
        // Validar que el negocio existe
        BarberShop shop = shopRepository.findById(req.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        // Verificar que el requester es dueño o barbero activo en el shop
        boolean isOwner = shop.getOwner().getId().equals(requesterId);
        Barber requesterBarber = barberRepository.findByUserId(requesterId).orElse(null);
        boolean isShopBarber = requesterBarber != null &&
                memberRepository.existsByShopIdAndBarberIdAndActiveTrue(req.getShopId(), requesterBarber.getId());

        if (!isOwner && !isShopBarber) {
            throw new IllegalArgumentException("No tienes permiso para agendar citas en este negocio");
        }

        // Barbero que realizará el servicio
        Barber barber = barberRepository.findById(req.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barbero no encontrado"));

        // Validar que el barbero seleccionado pertenece al shop
        if (!memberRepository.existsByShopIdAndBarberIdAndActiveTrue(req.getShopId(), barber.getId())) {
            throw new IllegalArgumentException("El barbero seleccionado no pertenece a este negocio");
        }

        ServiceEntity service = serviceRepository.findById(req.getServiceId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        // Cliente opcional (si tiene cuenta registrada)
        Profile clientProfile = null;
        if (req.getClientUserId() != null && !req.getClientUserId().isBlank()) {
            clientProfile = profileRepository.findById(req.getClientUserId()).orElse(null);
        }

        Appointment appointment = new Appointment();
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setUser(clientProfile);
        appointment.setDate(req.getDate());
        appointment.setTime(req.getTime());
        appointment.setClientName(req.getClientName());
        appointment.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : "cash");

        boolean isHome = "home".equals(req.getLocationType());
        appointment.setLocation(isHome ? "home" : "barbershop");
        appointment.setDurationMinutes(isHome ? 180 : 30);

        // Datos de domicilio
        if (isHome) {
            appointment.setClientAddress(req.getClientAddress());
            appointment.setClientLatitude(req.getClientLatitude());
            appointment.setClientLongitude(req.getClientLongitude());
            appointment.setHomeDistanceKm(req.getHomeDistanceKm());
        }

        int surcharge = req.getSurchargeAmount() != null ? req.getSurchargeAmount() : 0;
        appointment.setSurchargeAmount(surcharge);
        appointment.setTotalPrice(service.getPrice() + surcharge);
        appointment.setShopId(req.getShopId());
        appointment.setBookedByBarber(true);
        appointment.setStatus("confirmed");

        return appointmentRepository.save(appointment);
    }

    /**
     * Retorna IDs de barberos que tienen solapamiento de agenda.
     * Detecta conflictos considerando la duración de cada cita existente (30 min normal, 180 min domicilio).
     */
    public List<Long> getBookedBarberIds(String shopId, LocalDate date, LocalTime requestedTime, int requestedDurationMinutes) {
        List<Long> shopBarberIds = memberRepository.findByShopIdAndActiveTrue(shopId)
                .stream()
                .map(m -> m.getBarber().getId())
                .toList();

        if (shopBarberIds.isEmpty()) return List.of();

        // Obtener todas las citas activas del día para esos barberos
        List<Appointment> dayAppointments = appointmentRepository
                .findByBarberIdInAndDateAndStatusNot(shopBarberIds, date, "cancelled");

        LocalTime requestedEnd = requestedTime.plusMinutes(requestedDurationMinutes);

        return dayAppointments.stream()
                .filter(a -> {
                    LocalTime aStart = a.getTime();
                    int aDuration = (a.getDurationMinutes() != null && a.getDurationMinutes() > 0)
                            ? a.getDurationMinutes() : 30;
                    LocalTime aEnd = aStart.plusMinutes(aDuration);
                    // Hay solapamiento si: aStart < requestedEnd  Y  aEnd > requestedTime
                    return aStart.isBefore(requestedEnd) && aEnd.isAfter(requestedTime);
                })
                .map(a -> a.getBarber().getId())
                .distinct()
                .toList();
    }

    /**
     * Retorna todas las citas de una barbería (solo el dueño puede verlas).
     * Se obtienen buscando citas de todos los barberos activos del negocio.
     */
    @Transactional(readOnly = true)
    public List<Appointment> getShopAppointments(String shopId, String requesterId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(requesterId)) {
            throw new IllegalArgumentException("No tienes permiso para ver estas citas");
        }

        // IDs de barberos activos del negocio (para citas antiguas sin shopId)
        List<Long> barberIds = memberRepository.findByShopIdAndActiveTrue(shopId)
                .stream()
                .map(m -> m.getBarber().getId())
                .toList();

        if (barberIds.isEmpty()) barberIds = List.of(-1L); // evitar IN vacío

        // Trae citas con shopId asignado + citas legacy (shopId null) de esos barberos.
        // Usa JOIN FETCH para inicializar user/barber/service en la misma query
        // y evitar LazyInitializationException al serializar.
        return appointmentRepository.findByShopOrLegacyBarbersEager(shopId, barberIds);
    }

    /**
     * Confirma una cita pendiente (dueño o barbero del negocio).
     * Cambia estado: pending → confirmed
     */
    @Transactional
    public Appointment confirmAppointment(Long appointmentId, String requesterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!"pending".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden confirmar citas en estado pendiente");
        }

        boolean authorized = isOwnerOrBarberOfAppointment(appointment, requesterId);
        if (!authorized) {
            throw new RuntimeException("No tienes permiso para confirmar esta cita");
        }

        appointment.setStatus("confirmed");
        Appointment confirmed = appointmentRepository.save(appointment);

        // Notificar al cliente que su cita fue confirmada
        if (confirmed.getUser() != null) {
            try {
                String clientId = confirmed.getUser().getId();
                String clientEmail = appUserRepository.findById(clientId).map(AppUser::getEmail).orElse(null);
                String clientName  = confirmed.getUser().getFullName() != null ? confirmed.getUser().getFullName() : "Cliente";
                if (clientEmail != null) {
                    emailService.sendAppointmentConfirmed(clientEmail, clientName, buildEmailData(confirmed));
                }
            } catch (Exception ignored) {}
        }

        return confirmed;
    }

    /**
     * Marca una cita como "no show" — el cliente no se presentó.
     * Solo el dueño o barbero del negocio puede hacerlo.
     * Cambia estado: confirmed → no_show
     */
    @Transactional
    public Appointment noShowAppointment(Long appointmentId, String requesterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!"confirmed".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Solo se puede marcar 'no show' en citas confirmadas");
        }

        boolean authorized = isOwnerOrBarberOfAppointment(appointment, requesterId);
        if (!authorized) {
            throw new RuntimeException("No tienes permiso para modificar esta cita");
        }

        appointment.setStatus("no_show");
        return appointmentRepository.save(appointment);
    }

    /** Verifica si requesterId es dueño del negocio o el barbero de la cita */
    private boolean isOwnerOrBarberOfAppointment(Appointment appointment, String requesterId) {
        // ¿Es el dueño de algún shop que tiene a este barbero?
        boolean isOwner = memberRepository.findByBarberId(appointment.getBarber().getId())
                .stream()
                .anyMatch(m -> m.getShop().getOwner().getId().equals(requesterId) && m.getActive());

        if (isOwner) return true;

        // ¿Es el propio barbero de la cita?
        Barber requesterBarber = barberRepository.findByUserId(requesterId).orElse(null);
        return requesterBarber != null && requesterBarber.getId().equals(appointment.getBarber().getId());
    }

    /** Marca una cita como completada (dueño o barbero del negocio) */
    @Transactional
    public Appointment completeAppointment(Long appointmentId, String requesterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (!"confirmed".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden completar citas en estado confirmado");
        }

        if (!isOwnerOrBarberOfAppointment(appointment, requesterId)) {
            throw new RuntimeException("No tienes permiso para modificar esta cita");
        }

        appointment.setStatus("completed");
        Appointment completed = appointmentRepository.save(appointment);

        // Notificar al cliente que su cita fue completada
        if (completed.getUser() != null) {
            try {
                String clientId = completed.getUser().getId();
                String clientEmail = appUserRepository.findById(clientId).map(AppUser::getEmail).orElse(null);
                String clientName  = completed.getUser().getFullName() != null ? completed.getUser().getFullName() : "Cliente";
                if (clientEmail != null) {
                    emailService.sendAppointmentCompleted(clientEmail, clientName, buildEmailData(completed));
                }
            } catch (Exception ignored) {}
        }

        return completed;
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Devolver el corte a la suscripción si el pago fue con suscripción
        if ("subscription".equals(appointment.getPaymentMethod()) && appointment.getShopId() != null) {
            subscriptionService.refundCut(userId, appointment.getShopId());
        }

        appointment.setStatus("cancelled");
        Appointment cancelled = appointmentRepository.save(appointment);

        // Notificar al propietario que el cliente canceló
        if (cancelled.getShopId() != null) {
            try {
                String clientName = appointment.getUser() != null && appointment.getUser().getFullName() != null
                        ? appointment.getUser().getFullName() : "El cliente";
                shopRepository.findById(cancelled.getShopId()).ifPresent(shop -> {
                    AppUser owner = shop.getOwner();
                    String ownerName = profileRepository.findById(owner.getId())
                            .map(p -> p.getFullName() != null ? p.getFullName() : owner.getEmail())
                            .orElse(owner.getEmail());
                    emailService.sendAppointmentCancelledOwner(owner.getEmail(), ownerName, clientName, buildEmailData(cancelled));
                });
            } catch (Exception ignored) {}
        }

        return cancelled;
    }

    /**
     * Permite que el dueño o barbero del negocio cancele una cita
     * (ya sea pendiente o confirmada).
     * Si el pago era con suscripción, se devuelve el corte al cliente.
     */
    @Transactional
    public Appointment cancelByBarber(Long appointmentId, String requesterId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if ("completed".equals(appointment.getStatus()) || "cancelled".equals(appointment.getStatus())) {
            throw new IllegalArgumentException("No se puede cancelar una cita ya " + appointment.getStatus());
        }

        if (!isOwnerOrBarberOfAppointment(appointment, requesterId)) {
            throw new RuntimeException("No tienes permiso para cancelar esta cita");
        }

        // Devolver el corte a la suscripción si el pago fue con suscripción
        if ("subscription".equals(appointment.getPaymentMethod())
                && appointment.getShopId() != null
                && appointment.getUser() != null) {
            subscriptionService.refundCut(appointment.getUser().getId(), appointment.getShopId());
        }

        appointment.setStatus("cancelled");
        Appointment cancelledByBarber = appointmentRepository.save(appointment);

        // Notificar al cliente que el negocio canceló
        if (cancelledByBarber.getUser() != null) {
            try {
                String clientId = cancelledByBarber.getUser().getId();
                String clientEmail = appUserRepository.findById(clientId).map(AppUser::getEmail).orElse(null);
                String clientName  = cancelledByBarber.getUser().getFullName() != null ? cancelledByBarber.getUser().getFullName() : "Cliente";
                if (clientEmail != null) {
                    emailService.sendAppointmentCancelledClient(clientEmail, clientName, buildEmailData(cancelledByBarber));
                }
            } catch (Exception ignored) {}
        }

        return cancelledByBarber;
    }

    // ─── Helpers para emails ──────────────────────────────────────────────────

    private EmailService.AppointmentEmailData buildEmailData(Appointment a) {
        String shopName   = a.getShopId() != null
                ? shopRepository.findById(a.getShopId()).map(BarberShop::getName).orElse("—")
                : "—";
        String barberName = a.getBarber() != null ? a.getBarber().getName() : "—";
        String service    = a.getService() != null ? a.getService().getName() : "—";
        String date       = a.getDate() != null
                ? a.getDate().format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "CL")))
                : "—";
        String time       = a.getTime() != null
                ? a.getTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "—";
        String total      = a.getTotalPrice() != null ? "$" + a.getTotalPrice() : "—";
        return new EmailService.AppointmentEmailData(shopName, barberName, service, date, time, total);
    }
}
