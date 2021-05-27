package studyolle.account.application;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.account.dto.SignUpForm;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    public AccountService(AccountRepository accountRepository, JavaMailSender javaMailSender) {
        this.accountRepository = accountRepository;
        this.javaMailSender = javaMailSender;
    }

    public Account signUp(SignUpForm signUpForm) {
        return this.accountRepository.save(signUpForm.toAccount());
    }

    public void sendSignUpCheckEmail(Account account) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디올레 - 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + account.generateEmailCheckToken()
                                    + "&email=" + account.getEmail());
        this.javaMailSender.send(mailMessage);
    }
}
