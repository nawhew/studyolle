package studyolle.account.application;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.account.domain.security.UserAccount;
import studyolle.account.dto.SignUpForm;
import studyolle.settings.dto.NicknameForm;
import studyolle.settings.dto.Notifications;
import studyolle.settings.dto.Profile;
import studyolle.settings.dto.TagForm;
import studyolle.tag.domain.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AccountService implements UserDetailsService {

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

    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = this.findAccountByEmailOrNickname(emailOrNickname);
        if(account == null) {
            throw new UsernameNotFoundException("not found account : " + emailOrNickname);
        }
        return new UserAccount(account);
    }

    /**
     * 파라미터로 받은 값의 email 혹은 nickname을 가진 계정이 있는지 확인
     * @param emailOrNickname
     * @return
     */
    private Account findAccountByEmailOrNickname(String emailOrNickname) {
        Account account = null;
        if(this.accountRepository.findByEmail(emailOrNickname).isPresent()) {
            return this.accountRepository.findByEmail(emailOrNickname).get();
        }

        if(this.accountRepository.findByNickname(emailOrNickname).isPresent()) {
            account = this.accountRepository.findByNickname(emailOrNickname).get();
        }
        return account;
    }

    public Account findByNickname(String nickname) {
        Optional<Account> byNickname = this.accountRepository.findByNickname(nickname);
        if(byNickname.isEmpty()) {
            throw new IllegalArgumentException(nickname + "의 이름을 가진 유저가 없습니다.");
        }
        return byNickname.get();
    }

    public Account updateProfile(Account account, Profile profile) {
        return this.accountRepository.save(account.updateProfile(profile));
    }

    public void updatePassword(Account account, String password) {
        this.accountRepository.save(account.updateEncodedPassword(this.passwordEncoder, password));
    }

    public void updateNotifications(Account account, Notifications notifications) {
        this.accountRepository.save(account.updateNotifications(notifications));
    }

    public void updateNickname(Account account, NicknameForm nicknameForm) {
        Account updatedNicknameAccount = this.accountRepository.save(account.updateNickname(nicknameForm.getNickname()));
        this.login(updatedNicknameAccount);
    }

    public void addTag(Account account, Tag tag) {
        this.accountRepository.findById(account.getId())
                .ifPresent(account1 -> account1.addTag(tag));
    }

    public Set<Tag> findTags(Account account) {
        return this.accountRepository.findById(account.getId())
                .orElseThrow(IllegalArgumentException::new)
                .getTags();
    }

    public void removeTag(Account account, Tag tag) {
        this.accountRepository.findById(account.getId())
                .ifPresent(account1 -> account1.removeTag(tag));
    }
}
