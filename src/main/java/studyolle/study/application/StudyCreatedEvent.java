package studyolle.study.application;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import studyolle.study.domain.Study;

@Getter
public class StudyCreatedEvent {

    private Study study;

    public StudyCreatedEvent(Study persistStudy) {
        this.study = persistStudy;
    }
}
