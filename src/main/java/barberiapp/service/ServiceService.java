package barberiapp.service;

import barberiapp.dto.CreateServiceRequest;
import barberiapp.model.ApprovalStatus;
import barberiapp.model.BarberShop;
import barberiapp.model.ServiceEntity;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final BarberShopRepository shopRepository;

    /** Todos los servicios globales activos (legacy) */
    public List<ServiceEntity> getActiveServices() {
        return serviceRepository.findByActiveTrueOrActiveIsNull();
    }

    /** Servicios activos de una barbería específica */
    public List<ServiceEntity> getServicesByShop(String shopId) {
        return serviceRepository.findByShopIdAndActiveTrue(shopId);
    }

    /** Crear servicio para una barbería (valida que el requester sea dueño) */
    @Transactional
    public ServiceEntity createService(String shopId, String ownerId, CreateServiceRequest req) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este negocio");
        }

        if (shop.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("El negocio está pendiente de aprobación. El administrador debe aprobarlo antes de agregar servicios.");
        }
        if (shop.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("El negocio ha sido rechazado. No puedes agregar servicios.");
        }

        ServiceEntity service = new ServiceEntity();
        service.setName(req.getName());
        service.setDescription(req.getDescription());
        service.setPrice(req.getPrice());
        service.setDurationMinutes(req.getDurationMinutes());
        service.setShop(shop);
        service.setActive(true);

        return serviceRepository.save(service);
    }

    /** Actualizar un servicio existente de una barbería */
    @Transactional
    public ServiceEntity updateService(Long serviceId, String shopId, String ownerId, CreateServiceRequest req) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este negocio");
        }

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        if (service.getShop() == null || !service.getShop().getId().equals(shopId)) {
            throw new IllegalArgumentException("El servicio no pertenece a este negocio");
        }

        service.setName(req.getName());
        service.setDescription(req.getDescription());
        service.setPrice(req.getPrice());
        service.setDurationMinutes(req.getDurationMinutes());

        return serviceRepository.save(service);
    }

    /** Eliminar (soft delete) un servicio de una barbería */
    @Transactional
    public void deleteService(Long serviceId, String shopId, String ownerId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));

        if (!shop.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este negocio");
        }

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        if (service.getShop() == null || !service.getShop().getId().equals(shopId)) {
            throw new IllegalArgumentException("El servicio no pertenece a este negocio");
        }

        service.setActive(false);
        serviceRepository.save(service);
    }
}
