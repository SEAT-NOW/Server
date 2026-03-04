package depth.main.seatnow.domain.store.entity.menu;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sort_order")
    private Integer sortOrder; // 순서 필드 추가

    @Column(nullable = false)
    private String name; // 메뉴명

    @Column(nullable = false)
    private Integer price; // 가격

    private String imageUrl; // 메뉴 사진

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id")
    private MenuCategory menuCategory;

    @Column(nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Integer likeCount = 0;

    public void updateMenuDetails(String name, Integer price, String currentImageUrl, MenuCategory category) {
        this.name = name;
        this.price = price;
        this.imageUrl = currentImageUrl;
        this.menuCategory = category;
    }

    public void increaseLikeCount() {
        likeCount++;
    }

    public void decreaseLikeCount() {
        if (likeCount > 0) likeCount--;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
