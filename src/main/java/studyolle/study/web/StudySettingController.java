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
public class StudySettingController {

    private final StudyService studyService;

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

    /**
     * 스터디 배너 이미지 설정 폼 요청을 처리합니다.
     * @param account
     * @param path
     * @param model
     * @return
     */
    @GetMapping("/study/{path}/settings/banner")
    public String studyBannerSettingForm(@CurrentUserAccount Account account, @PathVariable String path
            , Model model) {
        model.addAttribute("account", account);
        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        if(!study.isManager(account)) {
            return "study/view";
        }
        return "study/settings/banner";
    }

    /**
     * 배너 이미지를 변경합니다.
     * @param account
     * @param path
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/study/{path}/settings/banner")
    public String updateStudyBannerImage(@CurrentUserAccount Account account, @PathVariable String path
            , String image, RedirectAttributes attributes) {
        this.studyService.updateStudyBannerImage(account, path, image);
        attributes.addFlashAttribute("message", "스터디 배너 이미지를 수정했습니다.");
        return "redirect:/study/" + path + "/settings/banner";
    }

    /**
     * 스터디에서 배너를 사용하도록 변경합니다.
     * @param account
     * @param path
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/study/{path}/settings/banner/enable")
    public String updateStudyBannerToEnable(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.updateStudyUseBanner(account, path, true);
        attributes.addFlashAttribute("message", "스터디에서 배너를 사용하도록 수정했습니다.");
        return "redirect:/study/" + path + "/settings/banner";
    }

    /**
     * 스터디에서 배너를 사용하지 않도록 변경합니다.
     * @param account
     * @param path
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping("/study/{path}/settings/banner/disable")
    public String updateStudyBannerToDisable(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.updateStudyUseBanner(account, path, false);
        attributes.addFlashAttribute("message", "스터디에서 배너를 사용하지 않도록 수정했습니다.");
        return "redirect:/study/" + path + "/settings/banner";
    }
}
