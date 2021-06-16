package studyolle.event.domain;

import lombok.*;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.UserAccount;
import studyolle.enrollment.domain.Enrollment;
import studyolle.study.domain.Study;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column
    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private final List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    /**
     * 처음 모임이 만들어 진 후 필요한 값을 채워줍니다.
     * @param study
     * @param account
     */
    public void init(Study study, Account account) {
        this.study = study;
        this.createdBy = account;
        this.createdDateTime = LocalDateTime.now();
    }

    /**
     * 참가 신청이 가능한지 여부 반환.
     * 모임이 열려있어야하고, 이미 참가되어있지 않아야함.
     * @param userAccount
     * @return
     */
    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    /**
     * 참가 취소가 가능한지 여부 반환.
     * 모임이 열려있어야하고, 이미 참가중이어야함.
     * @param userAccount
     * @return
     */
    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    /**
     * 참가 신청이 열려있는지 여부 반환.
     * 참가 신청 마감 기한이 현재 시간보다 후이면 열려있는 상태.
     * @return
     */
    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    /**
     * 참가 신청을 한 상태인지 여부 반환.
     * 매니저의 승인이 필요한 경우 아직 참가 확정이 안된 계정도 true 반환.
     * @param userAccount 
     * @return
     */
    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 참가 신청이 완료 되었는지 여부 반환.
     * 선착순이 아닌 경우 매니저의 승인까지 확정 된 계정만 true 반환.
     * @param userAccount 
     * @return
     */
    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }
}
