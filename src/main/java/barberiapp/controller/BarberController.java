package barberiapp.controller;

import barberiapp.model.Barber;
import barberiapp.service.BarberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @GetMapping
    public List<Barber> getBarbers() {
        return barberService.getActiveBarbers();
    }
}
