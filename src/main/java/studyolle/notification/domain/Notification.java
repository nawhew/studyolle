package studyolle.notification.domain;

import lombok.*;
import studyolle.account.domain.Account;
import studyolle.study.domain.Study;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account account;

    private LocalDateTime createdLocalDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    public static Notification create(Study study, Account account) {
        return Notification.builder()
                .title(study.getTitle())
                .link("/study/" + study.getPath())
                .checked(false)
                .createdLocalDateTime(LocalDateTime.now())
                .message(study.getShortDescription())
                .account(account)
                .notificationType(NotificationType.STUDY_CREATED)
                .build();
    }
}
