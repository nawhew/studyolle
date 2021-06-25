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
import studyolle.study.domain.StudyRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                        this.notificationRepository.save(Notification.create(study, account, study.getShortDescription()
                                , NotificationType.STUDY_CREATED));
                    }

                    if(account.isStudyCreatedByEmail()) {
                        // TODO 이메일 시스템 구축 후 이메일 알림 전송 추가
                    }
                });
    }

    @EventListener
    public void handleStudyUpdatedEvent(StudyUpdatedEvent studyUpdatedEvent) {
        Study study = studyUpdatedEvent.getStudy();
        String message = studyUpdatedEvent.getMessage();

        study.getMembers().stream()
                .forEach(account -> {
                    if(account.isStudyCreatedByWeb()) {
                        this.notificationRepository.save(Notification.create(study, account, message
                                , NotificationType.STUDY_UPDATED));
                    }

                    if(account.isStudyCreatedByEmail()) {
                        // TODO 이메일 시스템 구축 후 이메일 알림 전송 추가
                    }
                });
    }
}
