package studyolle.study.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import studyolle.study.domain.Study;

@Getter
@RequiredArgsConstructor
public class StudyCreatedEvent {

    private final Study study;
}
