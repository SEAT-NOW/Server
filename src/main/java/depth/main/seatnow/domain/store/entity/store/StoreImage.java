package depth.main.seatnow.domain.store.entity.store;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl; // S3에 저장된 이미지 URL

    @Column(nullable = false)
    private boolean isMain; // 대표 이미지 여부 (true면 대표)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    public static StoreImage create(String imageUrl, boolean isMain, Store store) {
        return StoreImage.builder()
                .imageUrl(imageUrl)
                .isMain(isMain)
                .store(store)
                .build();
    }

    public void updateMain(boolean main) {
        this.isMain = main;
    }
}
