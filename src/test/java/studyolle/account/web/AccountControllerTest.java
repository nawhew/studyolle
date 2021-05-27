package studyolle.account.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("회원가입 화면 요청 테스트")
    void signUpForm() throws Exception {
        this.mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUp() throws Exception {
        this.mockMvc.perform(post("/sign-up")
                            .param("email", "test@stutyolle.com")
                            .param("nickname", "test")
                            .param("password", "test1234")
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    @ParameterizedTest
    @CsvSource(value = {" ,test,test1234" // blank email
                        , "teststutyollecom,test,test1234" // not email format
                        , "test@stutyolle.com,te,test1234" // less than min length nickname
                        , "test@stutyolle.com,test,test123"}) // less than min length password
    @DisplayName("회원가입 잘못 된 요청 테스트")
    void signUp_bad_request(String email, String nickname, String password) throws Exception {
        this.mockMvc.perform(post("/sign-up")
                .param("email", email)
                .param("nickname", nickname)
                .param("password", password)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"));
    }
}