package depth.main.seatnow.global.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // JPA Entity들이 이 클래스를 상속받으면 필드(createdAt, modifiedAt)를 컬럼으로 인식하게 함
@EntityListeners(AuditingEntityListener.class) // 자동으로 시간을 입력해주는 기능 활성화
public class BaseTimeEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;
}
