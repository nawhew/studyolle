package studyolle.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import studyolle.study.domain.Study;

@Data
@AllArgsConstructor
public class StudyDescriptionForm {

    private String shortDescription;

    private String fullDescription;

    public static StudyDescriptionForm of(Study study) {
        return new StudyDescriptionForm(study.getShortDescription(), study.getFullDescription());
    }
}
