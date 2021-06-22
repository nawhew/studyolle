package studyolle.enrollment.domain;

import lombok.*;
import studyolle.account.domain.Account;
import studyolle.event.domain.Event;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

    public void accept() {
        this.accepted = true;
    }

    public void reject() {
        this.accepted = false;
    }

    public void checkIn() {
        if(this.isAccepted() && !this.isAttended()) {
            this.attended = true;
            return;
        }
        throw new IllegalArgumentException("출석 체크 할 수 없는 등록정보 입니다.");
    }

    public void cancelCheckIn() {
        if(this.isAccepted() && this.isAttended()) {
            this.attended = false;
            return;
        }
        throw new IllegalArgumentException("출석 취소 할 수 없는 등록정보 입니다.");
    }
}
