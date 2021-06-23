package studyolle.account.dto;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import studyolle.account.domain.AccountRepository;

@Component
public class NicknameFormValidator implements Validator {

    private final AccountRepository accountRepository;

    public NicknameFormValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
        if(this.accountRepository.findByNickname(nicknameForm.getNickname()).isPresent()) {
            errors.rejectValue("nickname", "wrong.value", "이미 사용중인 닉네임입니다.");
        }
    }
}
