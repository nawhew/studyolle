package studyolle.enrollment.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import studyolle.enrollment.domain.Enrollment;

@RequiredArgsConstructor @Getter
public class EnrollmentEvent {

    private final Enrollment enrollment;

    private final String message;
}
