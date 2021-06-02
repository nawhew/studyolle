package studyolle.account.application;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.account.domain.security.UserAccount;
import studyolle.account.dto.SignUpForm;

import java.time.LocalDateTime;
import java.util.List;
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
        this.accountRepository.save(account);
        this.javaMailSender.send(mailMessage);
    }

    /**
     * 생성 된 토큰을 이메일로 받아서 인증
     * @param token 
     * @param email
     * @return
     */
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

    /**
     * 자동 로그인을 위한 Account를 받아서 시큐리티 컨텍스트 홀더에 인증 토큰 추가
     * @param account 
     */
    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }
}
