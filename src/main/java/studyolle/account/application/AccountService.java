package studyolle.account.application;

import org.springframework.stereotype.Service;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.account.dto.SignUpForm;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account signUp(SignUpForm signUpForm) {
        return this.accountRepository.save(signUpForm.toAccount());
    }
}
