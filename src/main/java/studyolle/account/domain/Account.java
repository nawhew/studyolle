package studyolle.account.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id") @ToString(exclude = "password")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;
    private String emailCheckToken;
    private LocalDateTime joinedAt;

    private String bio;
    private String url;
    private String occupation;
    private String location;
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyEnrollmentResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    /**
     * 회원가입 체크 메일 토큰 생성
     * @return 
     */
    public String generateEmailCheckToken() {
        return UUID.randomUUID().toString();
    }
}
