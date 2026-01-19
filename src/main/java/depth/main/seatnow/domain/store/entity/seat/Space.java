package depth.main.seatnow.domain.store.entity.seat;

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
public class Space {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // 공간이 삭제되면 해당 공간의 테이블 설정도 함께 삭제됨
    @Builder.Default
    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableConfig> tableConfigs = new ArrayList<>();

    public static Space create(String name, Store store) {
        return Space.builder()
                .name(name)
                .store(store)
                .build();
    }

    public void updateName(String name) {
        this.name = name;
    }
}
