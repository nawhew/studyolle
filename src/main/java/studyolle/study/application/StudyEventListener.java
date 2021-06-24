package studyolle.study.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyolle.study.domain.Study;

@Component
@Async
@Transactional(readOnly = true)
@Slf4j
public class StudyEventListener {

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) {
        Study study = studyCreatedEvent.getStudy();
        log.info(study.getTitle() + " is created.");
    }
}
