package studyolle.enrollment.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.enrollment.domain.Enrollment;
import studyolle.enrollment.domain.EnrollmentRepository;
import studyolle.event.domain.Event;
import studyolle.notification.domain.Notification;
import studyolle.notification.domain.NotificationRepository;
import studyolle.notification.domain.NotificationType;

@Component
@Async
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;

    @EventListener
    public void enrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Notification notification = Notification.create(enrollment.getEvent(), account
                , enrollmentEvent.getMessage(), NotificationType.EVENT_ENROLLMENT);

        this.addNotification(account, notification);
    }

    private void addNotification(Account account, Notification notification) {
        if(account.isStudyEnrollmentResultByWeb()) {
            this.notificationRepository.save(notification);
        }
        if(account.isStudyEnrollmentResultByEmail()) {
            // TODO 메일 서비스 만들어 진 후 메일 보내기
        }
    }
}
