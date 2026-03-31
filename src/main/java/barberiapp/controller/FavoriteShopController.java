package barberiapp.controller;

import barberiapp.dto.FavoriteShopDto;
import barberiapp.service.FavoriteShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite-shops")
@RequiredArgsConstructor
public class FavoriteShopController {

    private final FavoriteShopService favoriteShopService;

    /** GET /api/favorite-shops?userId= — listar favoritos del usuario */
    @GetMapping
    public List<FavoriteShopDto> getFavoriteShops(@RequestParam String userId) {
        return favoriteShopService.getUserFavoriteShops(userId);
    }

    /** POST /api/favorite-shops/{shopId}?userId= — agregar shop a favoritos */
    @PostMapping("/{shopId}")
    public FavoriteShopDto addFavoriteShop(
            @RequestParam String userId,
            @PathVariable String shopId) {
        return favoriteShopService.addFavoriteShop(userId, shopId);
    }

    /** DELETE /api/favorite-shops/{id}?userId= — quitar de favoritos */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFavoriteShop(
            @PathVariable Long id,
            @RequestParam String userId) {
        favoriteShopService.removeFavoriteShop(id, userId);
        return ResponseEntity.noContent().build();
    }
}
