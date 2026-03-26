package barberiapp.controller;

import barberiapp.dto.*;
import barberiapp.repository.ProfileRepository;
import barberiapp.model.Profile;
import barberiapp.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;
    private final ProfileRepository profileRepository;

    // ── Admin: Events ─────────────────────────────────────────────────────────

    @GetMapping("/api/admin/shops/{shopId}/transport/events")
    public ResponseEntity<?> getEvents(@PathVariable String shopId) {
        try {
            String userId = getCurrentUserId();
            List<TransportEventResponse> events = transportService.getEvents(shopId, userId);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/shops/{shopId}/transport/events")
    public ResponseEntity<?> createEvent(@PathVariable String shopId,
                                          @RequestBody TransportEventRequest request) {
        try {
            String userId = getCurrentUserId();
            TransportEventResponse event = transportService.createEvent(shopId, request, userId);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/transport/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable Long eventId,
                                          @RequestParam String shopId,
                                          @RequestBody TransportEventRequest request) {
        try {
            String userId = getCurrentUserId();
            TransportEventResponse event = transportService.updateEvent(shopId, eventId, request, userId);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/transport/events/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId,
                                          @RequestParam String shopId) {
        try {
            String userId = getCurrentUserId();
            transportService.deleteEvent(shopId, eventId, userId);
            return ResponseEntity.ok(Map.of("message", "Evento eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Admin: Drivers ────────────────────────────────────────────────────────

    @GetMapping("/api/admin/shops/{shopId}/transport/drivers")
    public ResponseEntity<?> getDrivers(@PathVariable String shopId) {
        try {
            String userId = getCurrentUserId();
            List<TransportDriverResponse> drivers = transportService.getDrivers(shopId, userId);
            return ResponseEntity.ok(drivers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/shops/{shopId}/transport/drivers")
    public ResponseEntity<?> createDriver(@PathVariable String shopId,
                                           @RequestBody TransportDriverRequest request) {
        try {
            String userId = getCurrentUserId();
            TransportDriverResponse driver = transportService.createDriver(shopId, request, userId);
            return ResponseEntity.ok(driver);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/transport/drivers/{driverId}")
    public ResponseEntity<?> updateDriver(@PathVariable Long driverId,
                                           @RequestParam String shopId,
                                           @RequestBody TransportDriverRequest request) {
        try {
            String userId = getCurrentUserId();
            TransportDriverResponse driver = transportService.updateDriver(shopId, driverId, request, userId);
            return ResponseEntity.ok(driver);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/transport/drivers/{driverId}")
    public ResponseEntity<?> deleteDriver(@PathVariable Long driverId,
                                           @RequestParam String shopId) {
        try {
            String userId = getCurrentUserId();
            transportService.deleteDriver(shopId, driverId, userId);
            return ResponseEntity.ok(Map.of("message", "Conductor eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Admin: Vehicles ───────────────────────────────────────────────────────

    @GetMapping("/api/admin/shops/{shopId}/transport/vehicles")
    public ResponseEntity<?> getVehicles(@PathVariable String shopId) {
        try {
            String userId = getCurrentUserId();
            List<TransportVehicleResponse> vehicles = transportService.getVehicles(shopId, userId);
            return ResponseEntity.ok(vehicles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/shops/{shopId}/transport/vehicles")
    public ResponseEntity<?> createVehicle(@PathVariable String shopId,
                                            @RequestBody TransportVehicleRequest request) {
        try {
            String userId = getCurrentUserId();
            TransportVehicleResponse vehicle = transportService.createVehicle(shopId, request, userId);
            return ResponseEntity.ok(vehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/transport/vehicles/{vehicleId}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long vehicleId,
                                            @RequestParam String shopId,
                                            @RequestBody TransportVehicleRequest request) {
        try {
            String userId = getCurrentUserId();
            TransportVehicleResponse vehicle = transportService.updateVehicle(shopId, vehicleId, request, userId);
            return ResponseEntity.ok(vehicle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/transport/vehicles/{vehicleId}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId,
                                            @RequestParam String shopId) {
        try {
            String userId = getCurrentUserId();
            transportService.deleteVehicle(shopId, vehicleId, userId);
            return ResponseEntity.ok(Map.of("message", "Vehículo eliminado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Admin: Assignments ────────────────────────────────────────────────────

    @GetMapping("/api/admin/transport/events/{eventId}/assignments")
    public ResponseEntity<?> getEventAssignments(@PathVariable Long eventId) {
        try {
            String userId = getCurrentUserId();
            List<EventAssignmentResponse> assignments = transportService.getEventAssignments(eventId, userId);
            return ResponseEntity.ok(assignments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/transport/events/{eventId}/assignments")
    public ResponseEntity<?> assignVehicleToEvent(@PathVariable Long eventId,
                                                   @RequestBody EventAssignmentRequest request) {
        try {
            String userId = getCurrentUserId();
            EventAssignmentResponse assignment = transportService.assignVehicleToEvent(eventId, request, userId);
            return ResponseEntity.ok(assignment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/transport/assignments/{assignmentId}")
    public ResponseEntity<?> removeVehicleFromEvent(@PathVariable Long assignmentId) {
        try {
            String userId = getCurrentUserId();
            transportService.removeVehicleFromEvent(assignmentId, userId);
            return ResponseEntity.ok(Map.of("message", "Asignación eliminada"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Admin: Passengers ─────────────────────────────────────────────────────

    @GetMapping("/api/admin/transport/events/{eventId}/passengers")
    public ResponseEntity<?> getEventPassengers(@PathVariable Long eventId) {
        try {
            String userId = getCurrentUserId();
            List<PassengerBookingResponse> passengers = transportService.getEventPassengers(eventId, userId);
            return ResponseEntity.ok(passengers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/transport/bookings/{bookingId}/status")
    public ResponseEntity<?> updatePassengerStatus(@PathVariable Long bookingId,
                                                    @RequestBody Map<String, String> body) {
        try {
            String userId = getCurrentUserId();
            String status = body.get("status");
            if (status == null || status.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El campo 'status' es requerido"));
            }
            PassengerBookingResponse booking = transportService.updatePassengerStatus(bookingId, status, userId);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Public: Events & Assignments ──────────────────────────────────────────

    @GetMapping("/api/transport/shops/{slug}/events")
    public ResponseEntity<?> getPublicEventsByShop(@PathVariable String slug) {
        try {
            List<TransportEventResponse> events = transportService.getPublicEventsByShop(slug);
            return ResponseEntity.ok(events);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/transport/events/{eventId}/assignments")
    public ResponseEntity<?> getPublicAssignments(@PathVariable Long eventId) {
        try {
            List<EventAssignmentResponse> assignments = transportService.getPublicAssignments(eventId);
            return ResponseEntity.ok(assignments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Client: Bookings ──────────────────────────────────────────────────────

    @PostMapping("/api/transport/bookings")
    public ResponseEntity<?> bookPassengerSeat(@RequestBody PassengerBookingRequest request) {
        try {
            String userId = getCurrentUserId();
            String passengerName = profileRepository.findById(userId)
                    .map(Profile::getFullName)
                    .orElse("Pasajero");
            PassengerBookingResponse booking = transportService.bookPassengerSeat(request, userId, passengerName);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/transport/bookings/my")
    public ResponseEntity<?> getMyBookings() {
        try {
            String userId = getCurrentUserId();
            List<PassengerBookingResponse> bookings = transportService.getMyBookings(userId);
            return ResponseEntity.ok(bookings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/transport/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelMyBooking(@PathVariable Long bookingId) {
        try {
            String userId = getCurrentUserId();
            PassengerBookingResponse booking = transportService.cancelMyBooking(bookingId, userId);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
