package studyolle.account.domain;

import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import studyolle.settings.dto.Notifications;
import studyolle.settings.dto.Profile;

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
    private LocalDateTime emailCheckTokenGeneratedAt;
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
        String token = UUID.randomUUID().toString();
        this.emailCheckToken = token;
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
        return token;
    }

    /**
     * 비밀번호를 인코딩합니다.
     * @param passwordEncoder
     */
    public Account encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
        return this;
    }

    public Account updateEncodedPassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);
        return this;
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidEmailCheckToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendCheckEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    public Account updateProfile(Profile profile) {
        this.bio = profile.getBio();
        this.url = profile.getUrl();
        this.occupation = profile.getOccupation();
        this.location = profile.getLocation();
        this.profileImage = profile.getProfileImage();
        return this;
    }

    public Account updateNotifications(Notifications notifications) {
        this.studyCreatedByWeb = notifications.isStudyCreatedByWeb();
        this.studyCreatedByEmail = notifications.isStudyCreatedByEmail();
        this.studyUpdatedByWeb = notifications.isStudyUpdatedByWeb();
        this.studyUpdatedByEmail = notifications.isStudyUpdatedByEmail();
        this.studyEnrollmentResultByEmail = notifications.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = notifications.isStudyEnrollmentResultByWeb();
        return this;
    }
}
