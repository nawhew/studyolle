package studyolle.study.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import studyolle.study.domain.Study;

@RequiredArgsConstructor
@Getter
public class StudyUpdatedEvent {

    private final Study study;

    private final String message;
}
