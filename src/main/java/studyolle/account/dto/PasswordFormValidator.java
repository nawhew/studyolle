package studyolle.account.dto;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(PasswordForm.class);
    }

    /**
     * PasswordForm의 두 비밀번호가 일치하는지 확인
     * @param object
     * @param errors
     */
    @Override
    public void validate(Object object, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) object;
        if(!passwordForm.equalsPasswordAndConfirm()) {
            errors.rejectValue("newPassword", "wrong.value", "입력한 두 패스워드가 다릅니다.");
        }
    }
}
