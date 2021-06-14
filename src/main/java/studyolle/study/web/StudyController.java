package studyolle.study.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import java.net.URI;
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
        model.addAttribute("study", this.studyService.findByPath(path));
        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute("study", this.studyService.findByPath(path));
        return "study/members";
    }

    /**
     * 스터디 소개 설정 화면 요청
     * @param account 
     * @param path
     * @param model
     * @return
     */
    @GetMapping("/study/{path}/settings/description")
    public String studyDescriptionSettingForm(@CurrentUserAccount Account account, @PathVariable String path
            , Model model) {
        model.addAttribute("account", account);
        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        if(!study.isManager(account)) {
            return "study/view";
        }
        model.addAttribute("studyDescriptionForm", StudyDescriptionForm.of(study));
        return "study/settings/description";
    }

    /**
     * 스터디 소개 수정 요청
     * @param account 
     * @param path
     * @param studyDescriptionForm
     * @param model
     * @param errors
     * @return
     */
    @PostMapping("/study/{path}/settings/description")
    public String updateStudyDescription(@CurrentUserAccount Account account, @PathVariable String path
            , @Valid StudyDescriptionForm studyDescriptionForm, Errors errors, Model model
            , RedirectAttributes attributes) {
        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            return "study/settings/description";
        }

        this.studyService.updateStudyDescription(account, path, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + path + "/settings/description";
    }
}
