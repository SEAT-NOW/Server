package depth.main.seatnow.domain.store.entity.menu;

import depth.main.seatnow.domain.store.entity.store.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MenuCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder.Default
    @OneToMany(mappedBy = "menuCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Menu> menus = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
