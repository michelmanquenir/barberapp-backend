package barberiapp.dto;

import barberiapp.model.ProductCategory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Representa una categoría padre con sus subcategorías anidadas. */
@Data
@Builder
public class ProductCategoryTree {
    private String  id;
    private String  name;
    private String  icon;
    private String  parentId;
    private Integer sortOrder;
    private List<ProductCategoryTree> children;

    public static ProductCategoryTree from(ProductCategory cat, List<ProductCategoryTree> children) {
        return ProductCategoryTree.builder()
                .id(cat.getId())
                .name(cat.getName())
                .icon(cat.getIcon())
                .parentId(cat.getParentId())
                .sortOrder(cat.getSortOrder())
                .children(children)
                .build();
    }
}
