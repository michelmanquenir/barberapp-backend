package barberiapp.controller;

import barberiapp.model.BusinessCategory;
import barberiapp.repository.BusinessCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class BusinessCategoryController {

    private final BusinessCategoryRepository categoryRepository;

    /** Endpoint público — devuelve solo categorías activas ordenadas */
    @GetMapping
    public List<BusinessCategory> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc();
    }
}
