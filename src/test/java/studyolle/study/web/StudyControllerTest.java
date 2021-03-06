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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @WithAccount("newStudyForm")
    @DisplayName("스터디 개설 폼 요청 성공")
    void newStudyForm() throws Exception {
        // when - then
        this.mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "studyForm"))
                .andExpect(view().name("study/form"));
    }

    @Test
    @WithAccount("newStudy")
    @DisplayName("스터디 생성 성공")
    void newStudy() throws Exception {
        // given
        String path = "new-study-test";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        String nickname = "newStudy";

        // when - then
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when
        Study study = this.studyRepository.findByPath(path).get();
        Account account = this.accountRepository.findByNickname(nickname).get();

        // then
        assertThat(study).isNotNull();
        assertThat(study.getManagers()).contains(account);
        assertThat(study.getMembers()).contains(account);
        assertThat(study.getPath()).isEqualTo(path);
    }

    public static void 스터디_개설_요청_성공(MockMvc mockMvc, String path) throws Exception {
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        스터디_개설_요청_성공(mockMvc, path, title, shortDescription, fullDescription);
    }

    public static void 스터디_개설_요청_성공(MockMvc mockMvc, String path, String title
            , String shortDescription, String fullDescription) throws Exception {
        mockMvc.perform(post("/new-study")
                    .param("path", path)
                    .param("title", title)
                    .param("shortDescription", shortDescription)
                    .param("fullDescription", fullDescription)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + path));
    }

    @Test
    @WithAccount("studyView")
    @DisplayName("스터디 상세 화면 요청 성공")
    void studyView() throws Exception {
        // given
        String path = "new-study-test";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        String nickname = "newStudyForm";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(get("/study/" + path))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "study"))
                .andExpect(view().name("study/view"));
    }


    @Test
    @WithAccount("viewStudyMembers")
    @DisplayName("스터디 상세 화면 요청 성공")
    void viewStudyMembers() throws Exception {
        // given
        String path = "new-study-test";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";
        스터디_개설_요청_성공(this.mockMvc, path, title, shortDescription, fullDescription);

        // when - then
        this.mockMvc.perform(get("/study/" + path + "/members"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account", "study"))
                .andExpect(view().name("study/members"));
    }
}