package studyolle.account.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import studyolle.WithAccount;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.dto.TagForm;
import studyolle.account.dto.ZoneForm;
import studyolle.account.web.SettingsController;
import studyolle.tag.application.TagService;
import studyolle.tag.domain.Tag;
import studyolle.zone.application.ZoneService;
import studyolle.zone.domain.Zone;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @WithAccount("tagView")
    @DisplayName("관심 목록 뷰 및 리스트 조회 성공")
    void tagsSettingForm() throws Exception {
        // when - then
        this.mockMvc.perform(get(SettingsController.URL_SETTINGS_TAGS))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_TAGS))
                .andExpect(model().attributeExists("account", "tags", "whitelist"));
    }

    @Test
    @WithAccount("addTag")
    @DisplayName("관심 목록 추가 성공")
    void addTags() throws Exception {
        // given
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("tag-test");
        String nickname = "addTag";

        // when - then
        관심목록_추가_요청(tagForm);

        // when
        Tag tag = this.tagService.findByTitle(tagForm).get();
        Account account = this.accountService.findByNickname(nickname);

        // then
        assertThat(tag).isNotNull();
        assertThat(account.getTags()).contains(tag);
    }

    private void 관심목록_추가_요청(TagForm tagForm) throws Exception {
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_TAGS + "/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(tagForm))
                            .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithAccount("removeTags")
    @DisplayName("관심 목록 삭제 성공")
    void removeTags() throws Exception {
        // given
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("tag-test");
        String nickname = "removeTags";
        관심목록_추가_요청(tagForm);

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_TAGS + "/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(tagForm))
                            .with(csrf()))
                .andExpect(status().isOk());

        // when
        Tag tag = this.tagService.findByTitle(tagForm).get();
        Account account = this.accountService.findByNickname(nickname);

        // then
        assertThat(tag).isNotNull();
        assertThat(account.getTags()).doesNotContain(tag);
    }

    @Test
    @WithAccount("removeFailTags")
    @DisplayName("삭제를 요청한 태그가 없는 경우 관심목록 삭제 실패")
    void removeTags_fail_to_not_exists_tag() throws Exception {
        // given
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("notExistsTag");

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_TAGS + "/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(tagForm))
                            .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithAccount("zoneView")
    @DisplayName("활동지역 뷰 요청 성공")
    void zoneSettingForm() throws Exception {
        // when - then
        this.mockMvc.perform(get(SettingsController.URL_SETTINGS_ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingsController.VIEW_SETTINGS_ZONES))
                .andExpect(model().attributeExists("account", "zones", "whitelist"));
    }

    @Test
    @WithAccount("addZone")
    @DisplayName("유저에 활동 지역 추가 성공")
    void addZone() throws Exception {
        // given
        String nickname = "addZone";
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName("Gunpo(군포시)/Gyeonggi");

        // when - then
        활동지역_추가_요청(zoneForm);

        // when
        Zone zone = this.zoneService.findByZoneForm(zoneForm).get();
        Account account = this.accountService.findByNickname(nickname);

        // then
        assertThat(zone).isNotNull();
        assertThat(account.getZones()).contains(zone);
    }

    private void 활동지역_추가_요청(ZoneForm zoneForm) throws Exception {
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_ZONES + "/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(zoneForm))
                            .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithAccount("removeZone")
    @DisplayName("활동 지역 삭제 성공")
    void removeZone() throws Exception {
        // given
        String nickname = "removeZone";
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName("Gunpo(군포시)/Gyeonggi");
        활동지역_추가_요청(zoneForm);

        // when - then
        this.mockMvc.perform(post(SettingsController.URL_SETTINGS_ZONES + "/remove")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(this.objectMapper.writeValueAsString(zoneForm))
                            .with(csrf()))
                .andExpect(status().isOk());

        // when
        Zone zone = this.zoneService.findByZoneForm(zoneForm).get();
        Account account = this.accountService.findByNickname(nickname);

        // then
        assertThat(zone).isNotNull();
        assertThat(account.getZones()).doesNotContain(zone);
    }
}