package barberiapp.repository;

import barberiapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Carga una cita por ID con JOIN FETCH sobre user, barber y service
     * para evitar LazyInitializationException al serializar fuera de sesión
     * (open-in-view=false).
     */
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.barber " +
           "LEFT JOIN FETCH a.service " +
           "WHERE a.id = :id")
    Optional<Appointment> findByIdEager(@Param("id") Long id);
    List<Appointment> findByUserIdOrderByDateDescTimeDesc(String userId);

    /** Citas del cliente con eager fetch (para serializar sin sesión) */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.barber " +
           "LEFT JOIN FETCH a.service " +
           "WHERE a.user.id = :userId " +
           "ORDER BY a.date DESC, a.time DESC")
    List<Appointment> findByUserIdEager(@Param("userId") String userId);

    List<Appointment> findByUserIdAndStatusOrderByDateDescTimeDesc(String userId, String status);

    /**
     * Citas de un negocio: incluye las que tienen shopId asignado
     * Y las antiguas (shopId null) cuyos barberos pertenecen al negocio.
     * Evita duplicados con DISTINCT.
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "WHERE a.shopId = :shopId " +
           "   OR (a.shopId IS NULL AND a.barber.id IN :barberIds) " +
           "ORDER BY a.date DESC, a.time DESC")
    List<Appointment> findByShopOrLegacyBarbers(
            @Param("shopId") String shopId,
            @Param("barberIds") List<Long> barberIds);

    /**
     * Igual que findByShopOrLegacyBarbers pero con JOIN FETCH sobre
     * user, barber y service para evitar LazyInitializationException
     * al serializar fuera de sesión.
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.barber b " +
           "LEFT JOIN FETCH a.service " +
           "WHERE a.shopId = :shopId " +
           "   OR (a.shopId IS NULL AND b.id IN :barberIds) " +
           "ORDER BY a.date DESC, a.time DESC")
    List<Appointment> findByShopOrLegacyBarbersEager(
            @Param("shopId") String shopId,
            @Param("barberIds") List<Long> barberIds);

    /** Todas las citas de un conjunto de barberos */
    List<Appointment> findByBarberIdInOrderByDateDescTimeDesc(List<Long> barberIds);

    /**
     * Citas de UN barbero con eager fetch (para el dashboard de empleado).
     * Limita a fechas recientes/futuras para no sobrecargar.
     */
    @Query("SELECT DISTINCT a FROM Appointment a " +
           "LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.barber b " +
           "LEFT JOIN FETCH a.service " +
           "WHERE b.id = :barberId " +
           "AND a.date >= :since " +
           "ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findByBarberIdEagerSince(
            @Param("barberId") Long barberId,
            @Param("since") LocalDate since);

    /** Retorna los IDs de barberos que ya tienen cita (no cancelada) en una fecha y hora dadas (exacto) */
    @Query("SELECT a.barber.id FROM Appointment a " +
           "WHERE a.barber.id IN :barberIds " +
           "AND a.date = :date " +
           "AND a.time = :time " +
           "AND a.status != 'cancelled'")
    List<Long> findBookedBarberIds(
            @Param("barberIds") List<Long> barberIds,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );

    /** Todas las citas no canceladas de un conjunto de barberos en un día dado (para chequeo de solapamiento) */
    List<Appointment> findByBarberIdInAndDateAndStatusNot(List<Long> barberIds, LocalDate date, String status);

    /**
     * Todas las citas no canceladas de UN barbero en una fecha dada.
     * Usado para detección de conflictos entre negocios.
     */
    List<Appointment> findByBarberIdAndDateAndStatusNot(Long barberId, LocalDate date, String status);
}
