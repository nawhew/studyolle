package studyolle.study.web;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.study.application.StudyService;
import studyolle.study.domain.Study;
import studyolle.study.dto.StudyDescriptionForm;
import studyolle.study.dto.StudyForm;
import studyolle.study.dto.StudyFormValidator;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;
    private final StudyFormValidator studyFormValidator;

    @InitBinder("studyForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(this.studyFormValidator);
    }

    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("study", new Study());
        model.addAttribute("studyForm", new StudyForm());
        return "study/form";
    }

    @PostMapping("/new-study")
    public String newStudy(@CurrentUserAccount Account account, @Valid StudyForm studyForm
            , Errors errors, Model model) {
        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            return "study/form";
        }

        Study study = this.studyService.createStudy(account, studyForm);
        return "redirect:/study/" + URLEncoder.encode(study.getPath(), StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}")
    public String studyView(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("study", this.studyService.findByPath(path));
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute("study", this.studyService.findByPath(path));
        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute("study", this.studyService.joinStudy(account, path));
        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute("study", this.studyService.leaveStudy(account, path));
        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

    @GetMapping("/search/study")
    public String searchStudy(Model model, String keyword
        , @PageableDefault(size = 2, sort = "publishedDateTime", direction = Sort.Direction.DESC)
                          Pageable pageable) {
        model.addAttribute("studyPage", this.studyService.searchStudy(keyword, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",  pageable.getSort().toString().contains("publishedDateTime")
                                                ? "publishedDateTime"
                                                : "memberCount");
        return "search";
    }
}
