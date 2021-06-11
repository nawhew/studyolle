package studyolle.study.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import studyolle.study.domain.Study;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class StudyForm {

    @NotBlank
    @Length(min = 2, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9_-]{2,20}$")
    private String path;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;

    public Study toEntity() {
        return Study.builder()
                .path(this.path)
                .title(this.title)
                .shortDescription(this.shortDescription)
                .fullDescription(this.fullDescription)
                .build();
    }
}
