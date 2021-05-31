package studyolle.account.application;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.account.dto.SignUpForm;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    private final JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, JavaMailSender javaMailSender
            , PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.javaMailSender = javaMailSender;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 유저 정보 저장
     * @param signUpForm 
     * @return
     */
    public Account save(SignUpForm signUpForm) {
        return this.accountRepository.save(signUpForm.toAccount().encodePassword(this.passwordEncoder));
    }

    /**
     * 회원가입 인증 메일 전송
     * @param account 
     */
    public void sendSignUpCheckEmail(Account account) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(account.getEmail());
        mailMessage.setSubject("스터디올레 - 회원 가입 인증");
        mailMessage.setText("/check-email-token?token=" + account.generateEmailCheckToken()
                                    + "&email=" + account.getEmail());
        this.javaMailSender.send(mailMessage);
    }

    public Account checkEmailToken(String token, String email) {
        Optional<Account> account = this.accountRepository.findByEmail(email);
        Account checkedAccount = null;
        if(account.isPresent()
                && (checkedAccount = account.get()).isValidEmailCheckToken(token)) {
            checkedAccount.completeSignUp();
            return checkedAccount;
        }
        return null;
    }
}
