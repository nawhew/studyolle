package studyolle.notification.domain;

import lombok.*;
import studyolle.account.domain.Account;
import studyolle.event.domain.Event;
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

    private LocalDateTime createdDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    public static Notification create(Study study, Account account, String message, NotificationType notificationType) {
        return Notification.builder()
                .title(study.getTitle())
                .link("/study/" + study.getPath())
                .checked(false)
                .createdDateTime(LocalDateTime.now())
                .message(message)
                .account(account)
                .notificationType(notificationType)
                .build();
    }

    public static Notification create(Event event, Account account, String message, NotificationType notificationType) {
        return Notification.builder()
                .title(event.getTitle())
                .link("/study/" + event.getStudy().getPath() + "/events/" + event.getId())
                .checked(false)
                .createdDateTime(LocalDateTime.now())
                .message(message)
                .account(account)
                .notificationType(notificationType)
                .build();
    }

    public void checkAsRead() {
        this.checked = true;
    }
}
