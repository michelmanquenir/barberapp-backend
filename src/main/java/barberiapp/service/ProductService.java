package barberiapp.service;

import barberiapp.model.Product;
import barberiapp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }
}
