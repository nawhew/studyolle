package studyolle.settings.dto;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import studyolle.account.application.AccountService;
import studyolle.account.domain.AccountRepository;

@Component
public class NicknameFormValidator implements Validator {

    private final AccountService accountService;

    public NicknameFormValidator(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(NicknameForm.class);
    }

    /**
     * PasswordForm의 두 비밀번호가 일치하는지 확인
     * @param object
     * @param errors
     */
    @Override
    public void validate(Object object, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) object;
        try {
            this.accountService.findByNickname(nicknameForm.getNickname());
        } catch (IllegalArgumentException exception) {
            errors.rejectValue("nickname", "wrong.value", "이미 사용중인 닉네임입니다.");
        }
    }
}
