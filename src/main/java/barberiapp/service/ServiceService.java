package barberiapp.service;

import barberiapp.model.ServiceEntity;
import barberiapp.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public List<ServiceEntity> getActiveServices() {
        return serviceRepository.findByActiveTrue();
    }
}
