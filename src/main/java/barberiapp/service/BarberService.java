package barberiapp.service;

import barberiapp.model.Barber;
import barberiapp.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarberService {

    private final BarberRepository barberRepository;

    public List<Barber> getActiveBarbers() {
        return barberRepository.findByActiveTrue();
    }
}
