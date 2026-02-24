package barberiapp.repository;

import barberiapp.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserId(String userId);

    Optional<Favorite> findByUserIdAndBarberId(String userId, Long barberId);
}
