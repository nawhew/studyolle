package studyolle.study.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import studyolle.account.dto.SignUpForm;
import studyolle.study.domain.StudyRepository;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(StudyForm.class);
    }

    /**
     * SignUpForm의 email, nickname 값을 검증
     * rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage)
     * : 필드에 대한 에러코드를 추가, 메세지 인자로 errorArgs를 전달
     *     , 에러코드에 대한 메세지가 존재하지 않을 경우 defaultMessage사용
     * @param object
     * @param errors
     */
    @Override
    public void validate(Object object, Errors errors) {
        StudyForm studyForm = (StudyForm) object;
        if(this.studyRepository.existsByPath(studyForm.getPath())) {
            errors.rejectValue("path", "invalid.path", new Object[]{studyForm.getPath()}
                , "이미 사용중인 주소입니다.");
        }
    }
}
