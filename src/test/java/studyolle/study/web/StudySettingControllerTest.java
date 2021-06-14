package studyolle.study.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import studyolle.WithAccount;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.study.domain.Study;
import studyolle.study.domain.StudyRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static studyolle.study.web.StudyControllerTest.스터디_개설_요청_성공;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudySettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @WithAccount("studyDescriptionSettingForm")
    @DisplayName("스터디 설명 설정 화면 요청 성공")
    void studyDescriptionSettingForm() throws Exception {
        // given
        String path = "studyDescriptionSettingForm";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        String nickname = "newStudyForm";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(get("/study/" + path + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "study", "studyDescriptionForm"))
                .andExpect(view().name("study/settings/description"));
    }


    @Test
    @WithAccount("updateStudyDescription")
    @DisplayName("스터디 설명 수정 성공")
    void updateStudyDescription() throws Exception {
        // given
        String path = "updateStudyDescription";
        String title = "new-title";
        String shortDescription1 = "short desc 1";
        String fullDescription1 = "full desc 1";
        String shortDescription2 = "short desc 22";
        String fullDescription2 = "full desc 22";
        String requestUrl = "/study/" + path + "/settings/description";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription1, fullDescription1);

        // when - then
        this.mockMvc.perform(post(requestUrl)
                            .param("shortDescription", shortDescription2)
                            .param("fullDescription", fullDescription2)
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(requestUrl));

        // when
        Study study = this.studyRepository.findByPath(path).get();

        // then
        assertThat(study.getShortDescription()).isEqualTo(shortDescription2);
        assertThat(study.getFullDescription()).isEqualTo(fullDescription2);
    }


    @Test
    @WithAccount("studyBannerSettingForm")
    @DisplayName("스터디 설명 설정 화면 요청 성공")
    void studyBannerSettingForm() throws Exception {
        // given
        String path = "studyBannerSettingForm";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(get("/study/" + path + "/settings/banner"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "study"))
                .andExpect(view().name("study/settings/banner"));
    }

    @Test
    @WithAccount("updateStudyBanner")
    @DisplayName("스터디 배너 이미지 수정 성공")
    void updateStudyBanner() throws Exception {
        // given
        String path = "updateStudyBanner";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        String testUpdateImage = "iii";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(post("/study/" + path + "/settings/banner")
                .param("image", testUpdateImage)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + path + "/settings/banner"));

        // when
        Study study = this.studyRepository.findByPath(path).get();

        // then
        assertThat(study.getImage()).isEqualTo(testUpdateImage);
    }


    @Test
    @WithAccount("enableStudyBanner")
    @DisplayName("스터디 배너 이미지 사용 요청 성공")
    void enableStudyBanner() throws Exception {
        // given
        String path = "enableStudyBanner";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(post("/study/" + path + "/settings/banner/enable")
                            .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + path + "/settings/banner"));

        // when
        Study study = this.studyRepository.findByPath(path).get();

        // then
        assertTrue(study.isUseBanner());
    }

    @Test
    @WithAccount("disableStudyBanner")
    @DisplayName("스터디 배너 이미지 미사용 요청 성공")
    void disableStudyBanner() throws Exception {
        // given
        String path = "disableStudyBanner";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(post("/study/" + path + "/settings/banner/disable")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + path + "/settings/banner"));

        // when
        Study study = this.studyRepository.findByPath(path).get();

        // then
        assertFalse(study.isUseBanner());
    }
}