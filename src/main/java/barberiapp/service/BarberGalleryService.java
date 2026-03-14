package barberiapp.service;

import barberiapp.dto.GalleryImageRequest;
import barberiapp.dto.GalleryRequest;
import barberiapp.dto.GalleryResponse;
import barberiapp.model.Barber;
import barberiapp.model.BarberGallery;
import barberiapp.model.BarberGalleryImage;
import barberiapp.repository.BarberGalleryImageRepository;
import barberiapp.repository.BarberGalleryRepository;
import barberiapp.repository.BarberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BarberGalleryService {

    private final BarberGalleryRepository galleryRepository;
    private final BarberGalleryImageRepository imageRepository;
    private final BarberRepository barberRepository;

    // ── Endpoints privados (barbero autenticado) ──────────────────────────────

    /** Todas las galerías del barbero autenticado (incluso las ocultas) */
    public List<GalleryResponse> getMyGalleries(String userId) {
        Barber barber = getBarberOrThrow(userId);
        return galleryRepository.findByBarberIdOrderByCreatedAtDesc(barber.getId())
                .stream()
                .map(g -> GalleryResponse.from(g, false))
                .toList();
    }

    /** Galería con imágenes del barbero autenticado */
    public Optional<GalleryResponse> getGallery(String userId, Long galleryId) {
        Barber barber = getBarberOrThrow(userId);
        return galleryRepository.findById(galleryId)
                .filter(g -> g.getBarber().getId().equals(barber.getId()))
                .map(g -> GalleryResponse.from(g, true));
    }

    /** Crea una nueva galería */
    @Transactional
    public GalleryResponse createGallery(String userId, GalleryRequest req) {
        Barber barber = getBarberOrThrow(userId);
        BarberGallery gallery = new BarberGallery();
        gallery.setBarber(barber);
        gallery.setName(req.getName());
        gallery.setDescription(req.getDescription());
        gallery.setCoverUrl(req.getCoverUrl());
        gallery.setShopId(req.getShopId());
        gallery.setShopName(req.getShopName());
        gallery.setHidden(req.getHidden() != null ? req.getHidden() : false);
        return GalleryResponse.from(galleryRepository.save(gallery), false);
    }

    /** Actualiza una galería existente (partial update) */
    @Transactional
    public Optional<GalleryResponse> updateGallery(String userId, Long galleryId, GalleryRequest req) {
        Barber barber = getBarberOrThrow(userId);
        return galleryRepository.findById(galleryId)
                .filter(g -> g.getBarber().getId().equals(barber.getId()))
                .map(g -> {
                    if (req.getName() != null && !req.getName().isBlank())
                        g.setName(req.getName());
                    if (req.getDescription() != null)
                        g.setDescription(req.getDescription());
                    if (req.getCoverUrl() != null)
                        g.setCoverUrl(req.getCoverUrl());
                    // shopId: null explícito borra la asociación, valor la establece
                    if (req.getShopId() != null || req.getShopName() != null) {
                        g.setShopId(req.getShopId());
                        g.setShopName(req.getShopName());
                    }
                    if (req.getHidden() != null)
                        g.setHidden(req.getHidden());
                    return GalleryResponse.from(galleryRepository.save(g), false);
                });
    }

    /** Elimina una galería y sus imágenes */
    @Transactional
    public boolean deleteGallery(String userId, Long galleryId) {
        Barber barber = getBarberOrThrow(userId);
        return galleryRepository.findById(galleryId)
                .filter(g -> g.getBarber().getId().equals(barber.getId()))
                .map(g -> { galleryRepository.delete(g); return true; })
                .orElse(false);
    }

    /** Agrega una imagen a una galería */
    @Transactional
    public Optional<GalleryResponse.GalleryImageDto> addImage(String userId, Long galleryId, GalleryImageRequest req) {
        Barber barber = getBarberOrThrow(userId);
        return galleryRepository.findById(galleryId)
                .filter(g -> g.getBarber().getId().equals(barber.getId()))
                .map(g -> {
                    BarberGalleryImage image = new BarberGalleryImage();
                    image.setGallery(g);
                    image.setImageUrl(req.getImageUrl());
                    image.setCaption(req.getCaption());
                    if (g.getCoverUrl() == null || g.getCoverUrl().isBlank()) {
                        g.setCoverUrl(req.getImageUrl());
                        galleryRepository.save(g);
                    }
                    return GalleryResponse.GalleryImageDto.from(imageRepository.save(image));
                });
    }

    /** Elimina una imagen de una galería */
    @Transactional
    public boolean deleteImage(String userId, Long galleryId, Long imageId) {
        Barber barber = getBarberOrThrow(userId);
        return galleryRepository.findById(galleryId)
                .filter(g -> g.getBarber().getId().equals(barber.getId()))
                .flatMap(g -> imageRepository.findById(imageId)
                        .filter(img -> img.getGallery().getId().equals(galleryId)))
                .map(img -> { imageRepository.delete(img); return true; })
                .orElse(false);
    }

    // ── Endpoints públicos (cliente) ──────────────────────────────────────────

    /**
     * Galerías visibles (hidden=false) de un barbero.
     * Se pueden filtrar opcionalmente por shopId ("general" = sin shop).
     */
    public List<GalleryResponse> getPublicGalleries(Long barberId, String shopIdFilter) {
        List<BarberGallery> galleries =
                galleryRepository.findByBarberIdAndHiddenFalseOrderByCreatedAtDesc(barberId);

        if (shopIdFilter != null && !shopIdFilter.isBlank()) {
            if ("general".equalsIgnoreCase(shopIdFilter)) {
                galleries = galleries.stream()
                        .filter(g -> g.getShopId() == null)
                        .toList();
            } else {
                galleries = galleries.stream()
                        .filter(g -> shopIdFilter.equals(g.getShopId()))
                        .toList();
            }
        }

        return galleries.stream()
                .map(g -> GalleryResponse.from(g, true))
                .toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Barber getBarberOrThrow(String userId) {
        return barberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de barbero no encontrado"));
    }
}
