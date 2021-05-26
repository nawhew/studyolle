package studyolle.account.dto;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import studyolle.account.application.AccountService;
import studyolle.account.domain.AccountRepository;

@Component
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    public SignUpFormValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(SignUpForm.class);
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
        SignUpForm signUpForm = (SignUpForm) object;
        if(accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}
                , "이미 사용중인 이메일입니다.");
        }

        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}
            , "이미 사용중인 닉네임입니다.");
        }
    }
}
