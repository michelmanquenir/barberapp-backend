package barberiapp.repository;

import barberiapp.model.BusinessCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, String> {
    List<BusinessCategory> findByActiveTrueOrderBySortOrderAsc();
    boolean existsBySlug(String slug);
}
