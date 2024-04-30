package com.example.joindemo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberOption {

    @Id // pk = fk
    @Column(name = "member_id")
    private Long id;

    @MapsId
    @OneToOne
    @PrimaryKeyJoinColumn(name = "member_id")
    private Member member;

    private boolean isDeleted;

    private boolean isActivated;
}
