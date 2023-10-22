package com.example.account.domain;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// JPA Entity 클래스들이 해당 추상 클래스를 상속할 경우 createDate, modifiedDate를 컬럼으로 인식
@MappedSuperclass
// @CreatedDate @LastModifiedDate 어노테이션이 자동으로 값을 넣어줄 수 있게 함
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder                               // 자식 객체가 부모 객체의 필드를 빌더 패턴으로 사용할 수 있게 해준다.
public class BaseEntity {
    @Id                                     // Primary Key
    @GeneratedValue                         // Auto-Increase
    private Long id;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
