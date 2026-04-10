package barberiapp.service;

import barberiapp.dto.*;
import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportEventRepository eventRepository;
    private final TransportDriverRepository driverRepository;
    private final TransportVehicleRepository vehicleRepository;
    private final EventVehicleAssignmentRepository assignmentRepository;
    private final PassengerBookingRepository bookingRepository;
    private final BarberShopRepository barberShopRepository;
    private final ProfileRepository profileRepository;

    // ── Ownership helper ─────────────────────────────────────────────────────

    private BarberShop verifyOwnership(String shopId, String userId) {
        BarberShop shop = barberShopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        if (!shop.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Acceso denegado");
        }
        return shop;
    }

    // ── Events ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransportEventResponse> getEvents(String shopId, String userId) {
        verifyOwnership(shopId, userId);
        return eventRepository.findByShopIdOrderByEventDateAsc(shopId).stream()
                .map(e -> toEventResponse(e, assignmentRepository.findByEventId(e.getId()).size()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportEventResponse createEvent(String shopId, TransportEventRequest req, String userId) {
        verifyOwnership(shopId, userId);
        TransportEvent event = TransportEvent.builder()
                .shopId(shopId)
                .eventCode(req.getEventCode())
                .title(req.getTitle())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .eventDate(parseDateTime(req.getEventDate()))
                .bannerImageUrl(req.getBannerImageUrl())
                .pricePerKm(req.getPricePerKm())
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
        TransportEvent saved = eventRepository.save(event);
        return toEventResponse(saved, 0);
    }

    @Transactional
    public TransportEventResponse updateEvent(String shopId, Long eventId, TransportEventRequest req, String userId) {
        verifyOwnership(shopId, userId);
        TransportEvent event = eventRepository.findByIdAndShopId(eventId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (req.getEventCode() != null) event.setEventCode(req.getEventCode());
        if (req.getTitle() != null) event.setTitle(req.getTitle());
        if (req.getAddress() != null) event.setAddress(req.getAddress());
        if (req.getLatitude() != null) event.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) event.setLongitude(req.getLongitude());
        if (req.getEventDate() != null) event.setEventDate(parseDateTime(req.getEventDate()));
        if (req.getBannerImageUrl() != null) event.setBannerImageUrl(req.getBannerImageUrl());
        event.setPricePerKm(req.getPricePerKm()); // nullable — null = precio a convenir
        if (req.getActive() != null) event.setActive(req.getActive());
        TransportEvent saved = eventRepository.save(event);
        int vehicleCount = assignmentRepository.findByEventId(eventId).size();
        return toEventResponse(saved, vehicleCount);
    }

    @Transactional
    public void deleteEvent(String shopId, Long eventId, String userId) {
        verifyOwnership(shopId, userId);
        TransportEvent event = eventRepository.findByIdAndShopId(eventId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        // Cancel all bookings for assignments of this event
        List<EventVehicleAssignment> assignments = assignmentRepository.findByEventId(eventId);
        List<Long> assignmentIds = assignments.stream().map(EventVehicleAssignment::getId).collect(Collectors.toList());
        if (!assignmentIds.isEmpty()) {
            List<PassengerBooking> bookings = bookingRepository.findByAssignmentIdIn(assignmentIds);
            bookings.forEach(b -> b.setStatus("CANCELLED"));
            bookingRepository.saveAll(bookings);
            assignmentRepository.deleteAll(assignments);
        }
        eventRepository.delete(event);
    }

    // ── Drivers ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransportDriverResponse> getDrivers(String shopId, String userId) {
        verifyOwnership(shopId, userId);
        return driverRepository.findByShopIdOrderByNameAsc(shopId).stream()
                .map(this::toDriverResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportDriverResponse createDriver(String shopId, TransportDriverRequest req, String userId) {
        verifyOwnership(shopId, userId);
        TransportDriver driver = TransportDriver.builder()
                .shopId(shopId)
                .name(req.getName())
                .phone(req.getPhone())
                .licenseNumber(req.getLicenseNumber())
                .licenseImageUrl(req.getLicenseImageUrl())
                .notes(req.getNotes())
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
        return toDriverResponse(driverRepository.save(driver));
    }

    @Transactional
    public TransportDriverResponse updateDriver(String shopId, Long driverId, TransportDriverRequest req, String userId) {
        verifyOwnership(shopId, userId);
        TransportDriver driver = driverRepository.findByIdAndShopId(driverId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
        if (req.getName() != null) driver.setName(req.getName());
        if (req.getPhone() != null) driver.setPhone(req.getPhone());
        if (req.getLicenseNumber() != null) driver.setLicenseNumber(req.getLicenseNumber());
        if (req.getLicenseImageUrl() != null) driver.setLicenseImageUrl(req.getLicenseImageUrl());
        if (req.getNotes() != null) driver.setNotes(req.getNotes());
        if (req.getActive() != null) driver.setActive(req.getActive());
        return toDriverResponse(driverRepository.save(driver));
    }

    @Transactional
    public void deleteDriver(String shopId, Long driverId, String userId) {
        verifyOwnership(shopId, userId);
        TransportDriver driver = driverRepository.findByIdAndShopId(driverId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
        driverRepository.delete(driver);
    }

    // ── Vehicles ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransportVehicleResponse> getVehicles(String shopId, String userId) {
        verifyOwnership(shopId, userId);
        return vehicleRepository.findByShopIdOrderByBrandAsc(shopId).stream()
                .map(this::toVehicleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportVehicleResponse createVehicle(String shopId, TransportVehicleRequest req, String userId) {
        verifyOwnership(shopId, userId);
        TransportVehicle vehicle = TransportVehicle.builder()
                .shopId(shopId)
                .brand(req.getBrand())
                .model(req.getModel())
                .year(req.getYear())
                .licensePlate(req.getLicensePlate())
                .passengerCapacity(req.getPassengerCapacity() != null ? req.getPassengerCapacity() : 4)
                .commune(req.getCommune())
                .driverId(req.getDriverId())
                .imageUrl(req.getImageUrl())
                .active(req.getActive() != null ? req.getActive() : true)
                .build();
        return toVehicleResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public TransportVehicleResponse updateVehicle(String shopId, Long vehicleId, TransportVehicleRequest req, String userId) {
        verifyOwnership(shopId, userId);
        TransportVehicle vehicle = vehicleRepository.findByIdAndShopId(vehicleId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        if (req.getBrand() != null) vehicle.setBrand(req.getBrand());
        if (req.getModel() != null) vehicle.setModel(req.getModel());
        if (req.getYear() != null) vehicle.setYear(req.getYear());
        if (req.getLicensePlate() != null) vehicle.setLicensePlate(req.getLicensePlate());
        if (req.getPassengerCapacity() != null) vehicle.setPassengerCapacity(req.getPassengerCapacity());
        if (req.getCommune() != null) vehicle.setCommune(req.getCommune());
        vehicle.setDriverId(req.getDriverId()); // nullable: null = sin conductor asignado
        if (req.getImageUrl() != null) vehicle.setImageUrl(req.getImageUrl());
        if (req.getActive() != null) vehicle.setActive(req.getActive());
        return toVehicleResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(String shopId, Long vehicleId, String userId) {
        verifyOwnership(shopId, userId);
        TransportVehicle vehicle = vehicleRepository.findByIdAndShopId(vehicleId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        vehicleRepository.delete(vehicle);
    }

    // ── Assignments ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EventAssignmentResponse> getEventAssignments(Long eventId, String userId) {
        TransportEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        verifyOwnership(event.getShopId(), userId);
        return buildAssignmentResponses(assignmentRepository.findByEventId(eventId), event);
    }

    @Transactional
    public EventAssignmentResponse assignVehicleToEvent(Long eventId, EventAssignmentRequest req, String userId) {
        TransportEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        verifyOwnership(event.getShopId(), userId);

        // ── 1. El conductor no puede estar ya en ESTE mismo evento ───────────
        if (req.getDriverId() != null) {
            assignmentRepository.findByEventIdAndDriverId(eventId, req.getDriverId())
                    .ifPresent(a -> { throw new IllegalArgumentException(
                            "Este conductor ya está asignado a este evento"); });
        }

        // ── 2. El vehículo no puede estar ya en ESTE mismo evento ────────────
        assignmentRepository.findByEventIdAndVehicleId(eventId, req.getVehicleId())
                .ifPresent(a -> { throw new IllegalArgumentException(
                        "Este vehículo ya está asignado a este evento"); });

        // ── 3. El conductor no puede estar asignado a OTRO evento EL MISMO DÍA ─
        if (req.getDriverId() != null) {
            List<EventVehicleAssignment> driverAssignments = assignmentRepository.findByDriverId(req.getDriverId());
            boolean driverInOtherEventSameDay = driverAssignments.stream()
                    .filter(a -> !a.getEventId().equals(eventId))
                    .map(a -> eventRepository.findById(a.getEventId()))
                    .filter(opt -> opt.isPresent() && opt.get().getEventDate() != null)
                    .anyMatch(opt -> opt.get().getEventDate().toLocalDate()
                            .equals(event.getEventDate() != null ? event.getEventDate().toLocalDate() : null));
            if (driverInOtherEventSameDay) {
                String driverName = driverRepository.findById(req.getDriverId())
                        .map(d -> d.getName()).orElse("El conductor");
                throw new IllegalArgumentException(
                        driverName + " ya está asignado a otro evento el mismo día.");
            }
        }

        // ── 4. El vehículo no puede estar asignado a OTRO evento EL MISMO DÍA ─
        List<EventVehicleAssignment> vehicleAssignments = assignmentRepository.findByVehicleId(req.getVehicleId());
        boolean vehicleInOtherEventSameDay = vehicleAssignments.stream()
                .filter(a -> !a.getEventId().equals(eventId))
                .map(a -> eventRepository.findById(a.getEventId()))
                .filter(opt -> opt.isPresent() && opt.get().getEventDate() != null)
                .anyMatch(opt -> opt.get().getEventDate().toLocalDate()
                        .equals(event.getEventDate() != null ? event.getEventDate().toLocalDate() : null));
        if (vehicleInOtherEventSameDay) {
            TransportVehicle v = vehicleRepository.findById(req.getVehicleId()).orElse(null);
            String vName = v != null ? v.getBrand() + " " + v.getModel() : "El vehículo";
            throw new IllegalArgumentException(
                    vName + " ya está asignado a otro evento el mismo día.");
        }

        EventVehicleAssignment assignment = EventVehicleAssignment.builder()
                .eventId(eventId)
                .vehicleId(req.getVehicleId())
                .driverId(req.getDriverId())
                .build();
        EventVehicleAssignment saved = assignmentRepository.save(assignment);
        return toAssignmentResponse(saved, event);
    }

    @Transactional
    public void removeVehicleFromEvent(Long assignmentId, String userId) {
        EventVehicleAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));
        TransportEvent event = eventRepository.findById(assignment.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        verifyOwnership(event.getShopId(), userId);
        // Cancel related bookings
        List<PassengerBooking> bookings = bookingRepository.findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
        bookings.forEach(b -> b.setStatus("CANCELLED"));
        bookingRepository.saveAll(bookings);
        assignmentRepository.delete(assignment);
    }

    // ── Public endpoints ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TransportEventResponse> getPublicEventsByShop(String slug) {
        BarberShop shop = barberShopRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        // Solo mostrar eventos cuya fecha sea hoy o futura
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        return eventRepository.findByShopIdAndActiveOrderByEventDateAsc(shop.getId(), true).stream()
                .filter(e -> e.getEventDate() == null || !e.getEventDate().isBefore(startOfToday))
                .map(e -> toEventResponse(e, assignmentRepository.findByEventId(e.getId()).size()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventAssignmentResponse> getPublicAssignments(Long eventId) {
        TransportEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        return buildAssignmentResponses(assignmentRepository.findByEventId(eventId), event);
    }

    /**
     * Carga vehículos, conductores y conteo de asientos en 3 queries batch
     * en lugar de 3N queries individuales.
     */
    private List<EventAssignmentResponse> buildAssignmentResponses(
            List<EventVehicleAssignment> assignments, TransportEvent event) {

        if (assignments.isEmpty()) return List.of();

        // 1. Batch load vehicles
        List<Long> vehicleIds = assignments.stream()
                .map(EventVehicleAssignment::getVehicleId)
                .distinct().collect(Collectors.toList());
        Map<Long, TransportVehicle> vehicleMap = vehicleRepository.findAllById(vehicleIds).stream()
                .collect(Collectors.toMap(TransportVehicle::getId, v -> v));

        // 2. Batch load drivers
        List<Long> driverIds = assignments.stream()
                .map(EventVehicleAssignment::getDriverId)
                .filter(id -> id != null)
                .distinct().collect(Collectors.toList());
        Map<Long, TransportDriver> driverMap = driverIds.isEmpty() ? Map.of()
                : driverRepository.findAllById(driverIds).stream()
                        .collect(Collectors.toMap(TransportDriver::getId, d -> d));

        // 3. Batch load seat counts
        List<Long> assignmentIds = assignments.stream()
                .map(EventVehicleAssignment::getId).collect(Collectors.toList());
        Map<Long, Integer> seatsMap = new HashMap<>();
        for (Object[] row : bookingRepository.sumBookedSeatsByAssignmentIds(assignmentIds)) {
            Long aId = ((Number) row[0]).longValue();
            int seats  = ((Number) row[1]).intValue();
            seatsMap.put(aId, seats);
        }

        // 4. Map — zero extra queries
        return assignments.stream().map(a -> {
            TransportVehicle vehicle = vehicleMap.get(a.getVehicleId());
            TransportVehicleResponse vehicleResp = null;
            if (vehicle != null) {
                String driverName = a.getDriverId() != null
                        ? driverMap.getOrDefault(a.getDriverId(), null) != null
                                ? driverMap.get(a.getDriverId()).getName() : null
                        : null;
                vehicleResp = toVehicleResponseWithName(vehicle, driverName);
            }

            TransportDriverResponse driverResp = null;
            if (a.getDriverId() != null && driverMap.containsKey(a.getDriverId())) {
                driverResp = toDriverResponse(driverMap.get(a.getDriverId()));
            }

            int bookedSeats = seatsMap.getOrDefault(a.getId(), 0);
            int capacity = vehicle != null ? vehicle.getPassengerCapacity() : 0;

            return EventAssignmentResponse.builder()
                    .id(a.getId())
                    .eventId(a.getEventId())
                    .vehicle(vehicleResp)
                    .driver(driverResp)
                    .bookedSeats(bookedSeats)
                    .availableSeats(Math.max(0, capacity - bookedSeats))
                    .build();
        }).collect(Collectors.toList());
    }

    /** Igual que toVehicleResponse pero recibe driverName ya resuelto (sin query extra) */
    private TransportVehicleResponse toVehicleResponseWithName(TransportVehicle v, String driverName) {
        return TransportVehicleResponse.builder()
                .id(v.getId())
                .shopId(v.getShopId())
                .brand(v.getBrand())
                .model(v.getModel())
                .year(v.getYear())
                .licensePlate(v.getLicensePlate())
                .passengerCapacity(v.getPassengerCapacity())
                .commune(v.getCommune())
                .driverId(v.getDriverId())
                .driverName(driverName)
                .imageUrl(v.getImageUrl())
                .active(v.getActive())
                .createdAt(v.getCreatedAt())
                .build();
    }

    // ── Passenger bookings ───────────────────────────────────────────────────

    @Transactional
    public PassengerBookingResponse bookPassengerSeat(PassengerBookingRequest req, String userId, String passengerName) {
        EventVehicleAssignment assignment = assignmentRepository.findById(req.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));
        TransportVehicle vehicle = vehicleRepository.findById(assignment.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        TransportEvent event = eventRepository.findById(assignment.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        int seatsToBook = req.getSeatsBooked() != null && req.getSeatsBooked() > 0 ? req.getSeatsBooked() : 1;
        int bookedSeats = bookingRepository.sumBookedSeatsByAssignmentId(assignment.getId());
        int available = vehicle.getPassengerCapacity() - bookedSeats;

        if (seatsToBook > available) {
            throw new IllegalArgumentException("No hay suficientes asientos disponibles. Disponibles: " + available);
        }

        PassengerBooking booking = PassengerBooking.builder()
                .assignmentId(assignment.getId())
                .userId(userId)
                .passengerName(passengerName)
                .clientCommune(req.getClientCommune())
                .seatsBooked(seatsToBook)
                .status("PENDING")
                .notes(req.getNotes())
                .build();

        PassengerBooking saved = bookingRepository.save(booking);
        return toBookingResponse(saved, assignment, vehicle, event);
    }

    @Transactional(readOnly = true)
    public List<PassengerBookingResponse> getMyBookings(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(b -> {
                    EventVehicleAssignment assignment = assignmentRepository.findById(b.getAssignmentId()).orElse(null);
                    if (assignment == null) return null;
                    TransportVehicle vehicle = vehicleRepository.findById(assignment.getVehicleId()).orElse(null);
                    TransportEvent event = eventRepository.findById(assignment.getEventId()).orElse(null);
                    if (vehicle == null || event == null) return null;
                    return toBookingResponse(b, assignment, vehicle, event);
                })
                .filter(b -> b != null)
                .collect(Collectors.toList());
    }

    @Transactional
    public PassengerBookingResponse cancelMyBooking(Long bookingId, String userId) {
        PassengerBooking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalArgumentException("Solo se pueden cancelar reservas en estado PENDING. Estado actual: " + booking.getStatus());
        }
        booking.setStatus("CANCELLED");
        PassengerBooking saved = bookingRepository.save(booking);

        EventVehicleAssignment assignment = assignmentRepository.findById(saved.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));
        TransportVehicle vehicle = vehicleRepository.findById(assignment.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        TransportEvent event = eventRepository.findById(assignment.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        return toBookingResponse(saved, assignment, vehicle, event);
    }

    @Transactional(readOnly = true)
    public List<PassengerBookingResponse> getEventPassengers(Long eventId, String userId) {
        TransportEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        verifyOwnership(event.getShopId(), userId);
        List<EventVehicleAssignment> assignments = assignmentRepository.findByEventId(eventId);
        return assignments.stream()
                .flatMap(a -> {
                    TransportVehicle vehicle = vehicleRepository.findById(a.getVehicleId()).orElse(null);
                    if (vehicle == null) return java.util.stream.Stream.empty();
                    return bookingRepository.findByAssignmentIdOrderByCreatedAtDesc(a.getId()).stream()
                            .map(b -> toBookingResponse(b, a, vehicle, event));
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public PassengerBookingResponse updatePassengerStatus(Long bookingId, String status, String userId) {
        PassengerBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        EventVehicleAssignment assignment = assignmentRepository.findById(booking.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada"));
        TransportEvent event = eventRepository.findById(assignment.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        verifyOwnership(event.getShopId(), userId);

        booking.setStatus(status);
        PassengerBooking saved = bookingRepository.save(booking);
        TransportVehicle vehicle = vehicleRepository.findById(assignment.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        return toBookingResponse(saved, assignment, vehicle, event);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private TransportEventResponse toEventResponse(TransportEvent e, int vehicleCount) {
        return TransportEventResponse.builder()
                .id(e.getId())
                .shopId(e.getShopId())
                .eventCode(e.getEventCode())
                .title(e.getTitle())
                .address(e.getAddress())
                .latitude(e.getLatitude())
                .longitude(e.getLongitude())
                .eventDate(e.getEventDate())
                .bannerImageUrl(e.getBannerImageUrl())
                .pricePerKm(e.getPricePerKm())
                .active(e.getActive())
                .createdAt(e.getCreatedAt())
                .vehicleCount(vehicleCount)
                .build();
    }

    private TransportDriverResponse toDriverResponse(TransportDriver d) {
        return TransportDriverResponse.builder()
                .id(d.getId())
                .shopId(d.getShopId())
                .name(d.getName())
                .phone(d.getPhone())
                .licenseNumber(d.getLicenseNumber())
                .licenseImageUrl(d.getLicenseImageUrl())
                .notes(d.getNotes())
                .active(d.getActive())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private TransportVehicleResponse toVehicleResponse(TransportVehicle v) {
        String driverName = null;
        if (v.getDriverId() != null) {
            driverName = driverRepository.findById(v.getDriverId())
                    .map(TransportDriver::getName).orElse(null);
        }
        return TransportVehicleResponse.builder()
                .id(v.getId())
                .shopId(v.getShopId())
                .brand(v.getBrand())
                .model(v.getModel())
                .year(v.getYear())
                .licensePlate(v.getLicensePlate())
                .passengerCapacity(v.getPassengerCapacity())
                .commune(v.getCommune())
                .driverId(v.getDriverId())
                .driverName(driverName)
                .imageUrl(v.getImageUrl())
                .active(v.getActive())
                .createdAt(v.getCreatedAt())
                .build();
    }

    private EventAssignmentResponse toAssignmentResponse(EventVehicleAssignment a, TransportEvent event) {
        TransportVehicle vehicle = vehicleRepository.findById(a.getVehicleId()).orElse(null);
        TransportVehicleResponse vehicleResp = vehicle != null ? toVehicleResponse(vehicle) : null;

        TransportDriverResponse driverResp = null;
        if (a.getDriverId() != null) {
            driverResp = driverRepository.findById(a.getDriverId())
                    .map(this::toDriverResponse)
                    .orElse(null);
        }

        int bookedSeats = bookingRepository.sumBookedSeatsByAssignmentId(a.getId());
        int capacity = vehicle != null ? vehicle.getPassengerCapacity() : 0;
        int availableSeats = Math.max(0, capacity - bookedSeats);

        return EventAssignmentResponse.builder()
                .id(a.getId())
                .eventId(a.getEventId())
                .vehicle(vehicleResp)
                .driver(driverResp)
                .bookedSeats(bookedSeats)
                .availableSeats(availableSeats)
                .build();
    }

    private PassengerBookingResponse toBookingResponse(PassengerBooking b, EventVehicleAssignment assignment,
                                                        TransportVehicle vehicle, TransportEvent event) {
        EventAssignmentResponse assignmentResp = toAssignmentResponse(assignment, event);
        return PassengerBookingResponse.builder()
                .id(b.getId())
                .assignment(assignmentResp)
                .passengerName(b.getPassengerName())
                .clientCommune(b.getClientCommune())
                .seatsBooked(b.getSeatsBooked())
                .status(b.getStatus())
                .notes(b.getNotes())
                .createdAt(b.getCreatedAt())
                .eventTitle(event.getTitle())
                .eventDate(event.getEventDate())
                .eventAddress(event.getAddress())
                .build();
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ex) {
                log.warn("Could not parse date: {}", dateStr);
                return null;
            }
        }
    }
}
