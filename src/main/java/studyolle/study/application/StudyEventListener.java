package studyolle.study.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountPredicates;
import studyolle.account.domain.AccountRepository;
import studyolle.notification.domain.Notification;
import studyolle.notification.domain.NotificationRepository;
import studyolle.notification.domain.NotificationType;
import studyolle.study.domain.Study;

import java.time.LocalDateTime;

@Component
@Async
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudyEventListener {

    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyCreatedEvent.getStudy();
        this.accountRepository
                .findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()))
                .forEach(account -> {
                    if(account.isStudyCreatedByWeb()) {
                        this.notificationRepository.save(Notification.create(study, account));
                    }

                    if(account.isStudyCreatedByEmail()) {
                        // TODO 이메일 시스템 구축 후 이메일 알림 전송 추가
                    }
                });
    }
}
