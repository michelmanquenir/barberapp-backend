package barberiapp.repository;

import barberiapp.model.AppointmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentProductRepository extends JpaRepository<AppointmentProduct, Long> {

    /** Todos los ítems de productos de una cita */
    List<AppointmentProduct> findByAppointmentId(Long appointmentId);
}
