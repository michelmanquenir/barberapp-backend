package barberiapp.service;

import barberiapp.dto.ShopGalleryImageDto;
import barberiapp.model.BarberShop;
import barberiapp.model.ShopGalleryImage;
import barberiapp.repository.BarberShopRepository;
import barberiapp.repository.ShopGalleryImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopGalleryService {

    private static final int MAX_IMAGES = 20;

    private final ShopGalleryImageRepository imageRepository;
    private final BarberShopRepository shopRepository;

    // ── Público ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ShopGalleryImageDto> getImages(String shopId) {
        return imageRepository.findByShopIdOrderByDisplayOrderAscCreatedAtAsc(shopId)
                .stream()
                .map(ShopGalleryImageDto::from)
                .toList();
    }

    // ── Privado (dueño autenticado) ───────────────────────────────────────────

    @Transactional
    public ShopGalleryImageDto addImage(String userId, String shopId,
                                        String imageUrl, String caption) {
        verifyOwnership(shopId, userId);

        long count = imageRepository.countByShopId(shopId);
        if (count >= MAX_IMAGES) {
            throw new IllegalStateException("Límite de " + MAX_IMAGES + " fotos por negocio alcanzado");
        }

        ShopGalleryImage img = ShopGalleryImage.builder()
                .shopId(shopId)
                .imageUrl(imageUrl)
                .caption(caption)
                .displayOrder((int) count)
                .build();

        return ShopGalleryImageDto.from(imageRepository.save(img));
    }

    @Transactional
    public void deleteImage(String userId, String shopId, Long imageId) {
        verifyOwnership(shopId, userId);
        ShopGalleryImage img = imageRepository.findByIdAndShopId(imageId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));
        imageRepository.delete(img);
    }

    @Transactional
    public ShopGalleryImageDto updateCaption(String userId, String shopId,
                                              Long imageId, String caption) {
        verifyOwnership(shopId, userId);
        ShopGalleryImage img = imageRepository.findByIdAndShopId(imageId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));
        img.setCaption(caption);
        return ShopGalleryImageDto.from(imageRepository.save(img));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void verifyOwnership(String shopId, String userId) {
        BarberShop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no encontrado"));
        if (!shop.getOwner().getId().equals(userId)) {
            throw new SecurityException("No tienes permiso para modificar este negocio");
        }
    }
}
