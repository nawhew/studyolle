package studyolle.study.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.study.application.StudyService;
import studyolle.study.domain.Study;
import studyolle.study.dto.StudyForm;
import studyolle.study.dto.StudyFormValidator;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

        Optional<Study> study = this.studyService.findByPath(path);
        if(study.isEmpty()) {
            model.addAttribute("error", "해당 주소(" + path + ")의 스터디를 찾을 수 없습니다.");
            return "redirect:/";
        }
        model.addAttribute("study", study.get());
        return "study/view";
    }
}
