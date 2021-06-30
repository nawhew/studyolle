package studyolle.account.application;

import lombok.RequiredArgsConstructor;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.account.domain.security.UserAccount;
import studyolle.account.dto.SignUpForm;
import studyolle.account.dto.NicknameForm;
import studyolle.account.dto.Notifications;
import studyolle.account.dto.Profile;
import studyolle.common.email.EmailMessage;
import studyolle.common.email.EmailService;
import studyolle.config.AppProperties;
import studyolle.tag.domain.Tag;
import studyolle.zone.domain.Zone;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    private final PasswordEncoder passwordEncoder;

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
        this.accountRepository.save(account);

        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + account.generateEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
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

    public Set<Zone> findZones(Account account) {
        return this.accountRepository.findById(account.getId())
                .orElseThrow(IllegalArgumentException::new)
                .getZones();
    }

    public void addZone(Account account, Zone zone) {
        this.accountRepository.findById(account.getId())
                .ifPresent(account1 -> account1.addZone(zone));
    }

    public void removeZone(Account account, Zone zone) {
        this.accountRepository.findById(account.getId())
                .ifPresent(account1 -> account1.removeZone(zone));
    }

    public Account findWithTagsAndZonesById(Account account) {
        return this.accountRepository.findWithTagsAndZonesById(account.getId())
                .orElseThrow(IllegalAccessError::new);
    }
}
