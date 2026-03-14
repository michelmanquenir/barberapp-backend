package barberiapp.repository;

import barberiapp.model.BarberSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberScheduleRepository extends JpaRepository<BarberSchedule, Long> {

    /** Todos los horarios activos de un barbero (en todos sus negocios) */
    List<BarberSchedule> findByBarberIdAndActiveTrue(Long barberId);

    /** Horarios activos de todos los barberos de un negocio */
    List<BarberSchedule> findByShopIdAndActiveTrue(String shopId);

    /** Horarios activos de un barbero en un negocio específico */
    List<BarberSchedule> findByBarberIdAndShopIdAndActiveTrue(Long barberId, String shopId);

    /** Verifica si ya existe una entrada para ese barbero/negocio/día */
    boolean existsByBarberIdAndShopIdAndDayOfWeekAndActiveTrue(Long barberId, String shopId, String dayOfWeek);
}
