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
import static org.junit.jupiter.api.Assertions.*;
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
    @WithAccount("new-password")
    @DisplayName("비밀번호 수정 성공")
    void updatePassword() throws Exception {
        // given
        String newPassword = "password2";

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_PASSWORD)
                            .param("newPassword", newPassword)
                            .param("newPasswordConfirm", newPassword)
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.URL_SETTINGS_PASSWORD))
                .andExpect(flash().attributeExists("message"));

        // then
        Account account = this.accountService.findByNickname("new-password");
        assertThat(account.getPassword()).contains("{bcrypt}");
    }

    @Test
    @WithAccount("diff-password")
    @DisplayName("확인 비밀번호가 다른 경우 비밀번호 수정 실패")
    void updatePassword_fail_to_different_confirm() throws Exception {
        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_PASSWORD)
                            .param("newPassword", "12341234")
                            .param("newPasswordConfirm", "12345678")
                            .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_PASSWORD))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors());
    }


    @Test
    @WithAccount("noti")
    @DisplayName("알림설정 변경 성공")
    void updateNotifications() throws Exception {
        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_NOTIFICATIONS)
                            .param("studyCreatedByEmail", "true")
                            .param("studyCreatedByWeb", "true")
                            .param("studyEnrollmentResultByEmail", "true")
                            .param("studyEnrollmentResultByWeb", "true")
                            .param("studyUpdatedByEmail", "false")
                            .param("studyUpdatedByWeb", "false")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.URL_SETTINGS_NOTIFICATIONS))
                .andExpect(flash().attributeExists("message"));

        // then
        Account account = this.accountService.findByNickname("noti");
        assertTrue(account.isStudyCreatedByEmail());
        assertTrue(account.isStudyCreatedByWeb());
        assertTrue(account.isStudyEnrollmentResultByEmail());
        assertTrue(account.isStudyEnrollmentResultByWeb());
        assertFalse(account.isStudyUpdatedByEmail());
        assertFalse(account.isStudyUpdatedByWeb());
    }


    @Test
    @WithAccount("nickname1")
    @DisplayName("닉네임 수정 성공")
    void updateNickname() throws Exception {
        // given
        String originNickname = "nickname1";
        String updateNickname = "nickname2";

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_ACCOUNT)
                            .param("nickname", updateNickname)
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingsController.URL_SETTINGS_ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        // then
        Account account = this.accountService.findByNickname(updateNickname);
        assertThat(account).isNotNull();

        // when
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            this.accountService.findByNickname(originNickname);
        });

        // then
        assertEquals(exception.getMessage(), originNickname + "의 이름을 가진 유저가 없습니다.");
    }

    @Test
    @WithAccount("exitNickname")
    @DisplayName("이미 있는 닉네임으로 수정 요청시 실패")
    void updateNickname_fail_to_exit_nickname() throws Exception {
        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_ACCOUNT)
                            .param("nickname", "exitNickname")
                            .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_ACCOUNT))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"))
                .andExpect(model().hasErrors());
    }
}