package studyolle.account.web.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("회원가입 화면 요청 테스트")
    void signUpForm() throws Exception {
        // when - then
        this.mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUp() throws Exception {
        // given
        String email = "test@stutyolle.com";
        String nickname = "test";

        // when - then
        회원가입_요청(email, nickname);

        // then
        this.회원가입_인증_메일_전송_확인();
        this.회원가입_비밀번호_인코딩_확인(email);
    }

    private void 회원가입_요청(String email, String nickname) throws Exception {
        this.mockMvc.perform(post("/sign-up")
                            .param("email", email)
                            .param("nickname", nickname)
                            .param("password", "test1234")
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername(nickname));
    }

    private void 회원가입_인증_메일_전송_확인() {
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    private void 회원가입_비밀번호_인코딩_확인(String email) {
        Account newAccount = this.accountRepository.findByEmail(email).get();
        assertThat(newAccount.getPassword()).isNotEqualTo(email);
    }

    @ParameterizedTest
    @CsvSource(value = {" ,test,test1234" // blank email
                        , "teststutyollecom,test,test1234" // not email format
                        , "test@stutyolle.com,te,test1234" // less than min length nickname
                        , "test@stutyolle.com,test,test123"}) // less than min length password
    @DisplayName("회원가입 잘못 된 요청 테스트")
    void signUp_bad_request(String email, String nickname, String password) throws Exception {
        // when - then
        this.mockMvc.perform(post("/sign-up")
                .param("email", email)
                .param("nickname", nickname)
                .param("password", password)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원가입 이메일 토큰 체크")
    void checkEmailToken() throws Exception {
        // given
        String email = "test@email.com";
        String nickname = "test-token";
        회원가입_요청(email, nickname);
        String token = this.accountRepository.findByEmail(email).get().getEmailCheckToken();

        // when - then
        this.mockMvc.perform(get("/check-email-token")
                            .param("email", email)
                            .param("token", token)
                            .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(model().attribute("nickname", nickname))
                .andExpect(authenticated().withUsername(nickname));
    }


    @Test
    @DisplayName("회원가입 이메일 비정상 토큰")
    void checkEmailToken_wrong_token() throws Exception {
        // given
        String email = "wrong-token@email.com";
        String nickname = "test-wrong-token";
        회원가입_요청(email, nickname);
        Account account = this.accountRepository.findByEmail(email).get();
        String wrongToken = account.getEmailCheckToken() + "abc";

        // when - then
        this.mockMvc.perform(get("/check-email-token")
                            .param("email", email)
                            .param("token", wrongToken)
                            .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/checked-email"))
                .andExpect(model().attributeExists("error"))
                .andExpect(unauthenticated());
    }
}