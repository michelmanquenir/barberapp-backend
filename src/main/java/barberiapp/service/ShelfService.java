package barberiapp.service;

import barberiapp.dto.*;
import barberiapp.model.Product;
import barberiapp.model.Shelf;
import barberiapp.model.ShelfSlot;
import barberiapp.repository.ProductRepository;
import barberiapp.repository.ShelfRepository;
import barberiapp.repository.ShelfSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShelfService {

    private final ShelfRepository shelfRepository;
    private final ShelfSlotRepository shelfSlotRepository;
    private final ProductRepository productRepository;

    // ── Consultas ───────────────────────────────────────────────────────────────

    /** Lista todas las estanterías de un negocio con el conteo de slots ocupados. */
    @Transactional(readOnly = true)
    public List<ShelfResponse> getShelves(String shopId) {
        List<Shelf> shelves = shelfRepository.findByShopIdOrderByNameAsc(shopId);
        if (shelves.isEmpty()) return Collections.emptyList();

        // Contar slots distintos ocupados por estantería (un slot puede tener varios productos)
        List<Product> withSlot = productRepository.findByShopIdWithAssignedSlot(shopId);
        Map<Long, Set<Long>> slotsByShelf = withSlot.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getShelfSlot().getShelf().getId(),
                        Collectors.mapping(p -> p.getShelfSlot().getId(), Collectors.toSet())
                ));

        return shelves.stream().map(s -> {
            ShelfResponse r = ShelfResponse.from(s);
            r.setOccupiedSlots(slotsByShelf.getOrDefault(s.getId(), Collections.emptySet()).size());
            return r;
        }).toList();
    }

    /** Devuelve la grilla completa de una estantería con el producto en cada slot. */
    @Transactional(readOnly = true)
    public ShelfGridResponse getGrid(String shopId, Long shelfId) {
        Shelf shelf = shelfRepository.findByIdAndShopId(shelfId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Estantería no encontrada"));

        List<ShelfSlot> slots = shelfSlotRepository.findByShelfIdOrderByCodeAsc(shelfId);
        List<Product> products = productRepository.findByShopIdAndShelfId(shopId, shelfId);

        // Mapa slotId → lista de productos (un slot puede tener varios)
        Map<Long, List<Product>> bySlot = products.stream()
                .collect(Collectors.groupingBy(p -> p.getShelfSlot().getId()));

        List<ShelfSlotResponse> slotResponses = slots.stream().map(slot -> {
            ShelfSlotResponse sr = ShelfSlotResponse.from(slot);
            List<Product> slotProducts = bySlot.getOrDefault(slot.getId(), Collections.emptyList());
            sr.setProducts(slotProducts.stream().map(prod -> {
                ShelfSlotResponse.SlotProduct sp = new ShelfSlotResponse.SlotProduct();
                sp.setProductId(prod.getId());
                sp.setProductName(prod.getResolvedName());
                sp.setProductImageUrl(prod.getResolvedImageUrl());
                sp.setProductStock(prod.getStock());
                sp.setProductSalePrice(prod.getSalePrice());
                return sp;
            }).toList());
            return sr;
        }).toList();

        ShelfGridResponse response = new ShelfGridResponse();
        response.setId(shelf.getId());
        response.setName(shelf.getName());
        response.setDescription(shelf.getDescription());
        response.setRows(shelf.getRows());
        response.setColumns(shelf.getColumns());
        response.setSlots(slotResponses);
        return response;
    }

    // ── CRUD ────────────────────────────────────────────────────────────────────

    /** Crea una estantería y genera automáticamente sus slots (A1, A2... Z10). */
    @Transactional
    public ShelfResponse createShelf(String shopId, ShelfRequest req) {
        validateRequest(req);

        Shelf shelf = new Shelf();
        shelf.setShopId(shopId);
        shelf.setName(req.getName().trim());
        shelf.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        shelf.setRows(req.getRows());
        shelf.setColumns(req.getColumns());
        shelf = shelfRepository.save(shelf);

        // Generar slots A1, A2... según filas × columnas
        List<ShelfSlot> slots = new ArrayList<>();
        for (int r = 0; r < req.getRows(); r++) {
            char rowChar = (char) ('A' + r);
            for (int c = 1; c <= req.getColumns(); c++) {
                ShelfSlot slot = new ShelfSlot();
                slot.setShelf(shelf);
                slot.setCode(String.valueOf(rowChar) + c);
                slots.add(slot);
            }
        }
        shelfSlotRepository.saveAll(slots);

        ShelfResponse r = ShelfResponse.from(shelf);
        r.setOccupiedSlots(0);
        return r;
    }

    /** Actualiza nombre y descripción de una estantería. */
    @Transactional
    public ShelfResponse updateShelf(String shopId, Long shelfId, ShelfRequest req) {
        Shelf shelf = shelfRepository.findByIdAndShopId(shelfId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Estantería no encontrada"));

        if (req.getName() != null && !req.getName().isBlank()) {
            shelf.setName(req.getName().trim());
        }
        if (req.getDescription() != null) {
            shelf.setDescription(req.getDescription().trim().isEmpty() ? null : req.getDescription().trim());
        }

        shelfRepository.save(shelf);
        ShelfResponse r = ShelfResponse.from(shelf);
        // Contar slots distintos ocupados
        long occupied = productRepository.findByShopIdWithAssignedSlot(shopId).stream()
                .filter(p -> p.getShelfSlot().getShelf().getId().equals(shelfId))
                .map(p -> p.getShelfSlot().getId())
                .distinct()
                .count();
        r.setOccupiedSlots((int) occupied);
        return r;
    }

    /** Elimina una estantería. Primero desvincula los productos que tenían slots en ella. */
    @Transactional
    public void deleteShelf(String shopId, Long shelfId) {
        Shelf shelf = shelfRepository.findByIdAndShopId(shelfId, shopId)
                .orElseThrow(() -> new IllegalArgumentException("Estantería no encontrada"));

        // Desvincular productos que apuntaban a slots de esta estantería
        List<Product> assigned = productRepository.findByShopIdAndShelfId(shopId, shelfId);
        assigned.forEach(p -> p.setShelfSlot(null));
        if (!assigned.isEmpty()) productRepository.saveAll(assigned);

        shelfRepository.delete(shelf);
    }

    /** Actualiza la etiqueta libre de un slot. */
    @Transactional
    public ShelfSlotResponse updateSlotLabel(String shopId, Long slotId, String label) {
        ShelfSlot slot = shelfSlotRepository.findByIdWithShelf(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada"));
        if (!slot.getShelf().getShopId().equals(shopId)) {
            throw new IllegalArgumentException("La posición no pertenece a este negocio");
        }
        slot.setLabel(label == null || label.isBlank() ? null : label.trim());
        return ShelfSlotResponse.from(shelfSlotRepository.save(slot));
    }

    // ── helper ──────────────────────────────────────────────────────────────────

    private void validateRequest(ShelfRequest req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("El nombre de la estantería es obligatorio");
        if (req.getRows() < 1 || req.getRows() > 26)
            throw new IllegalArgumentException("Las filas deben ser entre 1 y 26 (A–Z)");
        if (req.getColumns() < 1 || req.getColumns() > 50)
            throw new IllegalArgumentException("Las columnas deben ser entre 1 y 50");
    }
}
