package studyolle.settings.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import studyolle.WithAccount;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static studyolle.account.web.account.AccountControllerTestSupport.회원가입_요청;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Test
    @WithAccount("long-bio")
    @DisplayName("소개가 긴 경우 프로필 수정 실패")
    void updateProfile_fail_to_longBio() throws Exception {
        // given
        String bio = "소개를 수정하기에 소개가 너무 너무 너무 너무 너무 깁니다. 소개는 35자 이하여야 합니다.";

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_PROFILE)
                            .param("bio", bio)
                            .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithAccount("nawhew")
    @DisplayName("프로필 수정 성공")
    void updateProfile() throws Exception {
        // given
        String bio = "소개 수정을 성공합니다.";

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_PROFILE)
                            .param("bio", bio)
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.URL_SETTINGS_PROFILE))
                .andExpect(flash().attributeExists("message"));

        // then
        Account account = this.accountService.findByNickname("nawhew");
        assertThat(account.getBio()).isEqualTo(bio);
    }
}