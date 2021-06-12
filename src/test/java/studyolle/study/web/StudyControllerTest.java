package studyolle.study.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import studyolle.WithAccount;
import studyolle.study.domain.StudyRepository;

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
    @WithAccount("newStudyForm")
    @DisplayName("스터디 생성 성공")
    void newStudy() throws Exception {
        // given
        String path = "new-study-test";
        String title = "new-title";
        String shortDescription = "short desc";
        String fullDescription = "full desc";

        // when - then
        this.mockMvc.perform(post("/new-study")
                    .param("path", path)
                    .param("title", title)
                    .param("shortDescription", shortDescription)
                    .param("fullDescription", fullDescription)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // when
        boolean existsByPath = this.studyRepository.existsByPath(path);

        // then
        assertTrue(existsByPath);
    }
}