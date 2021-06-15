package studyolle.study.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import studyolle.settings.dto.TagForm;
import studyolle.settings.dto.ZoneForm;
import studyolle.study.application.StudyService;
import studyolle.study.domain.Study;
import studyolle.study.dto.StudyDescriptionForm;
import studyolle.study.dto.StudyForm;
import studyolle.study.dto.StudyFormValidator;
import studyolle.tag.application.TagService;
import studyolle.tag.domain.Tag;
import studyolle.zone.application.ZoneService;
import studyolle.zone.domain.Zone;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingController {

    private final StudyService studyService;
    private final TagService tagService;
    private final ZoneService zoneService;
    private final ObjectMapper objectMapper;

    /**
     * 스터디 소개 설정 화면 요청
     * @param account 
     * @param path
     * @param model
     * @return
     */
    @GetMapping("/description")
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
    @PostMapping("/description")
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
    @GetMapping("/banner")
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
    @PostMapping("/banner")
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
    @PostMapping("/banner/enable")
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
    @PostMapping("/banner/disable")
    public String updateStudyBannerToDisable(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.updateStudyUseBanner(account, path, false);
        attributes.addFlashAttribute("message", "스터디에서 배너를 사용하지 않도록 수정했습니다.");
        return "redirect:/study/" + path + "/settings/banner";
    }


    @GetMapping("/tags")
    public String studyTagSettingForm(@CurrentUserAccount Account account, @PathVariable String path
            , Model model) throws JsonProcessingException {
        model.addAttribute("account", account);
        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        model.addAttribute("tags", study.getTagTitles());

        List<String> allTags = this.tagService.findAllTags().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", this.objectMapper.writeValueAsString(allTags));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    public @ResponseBody ResponseEntity addTag(@CurrentUserAccount Account account, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        this.studyService.addTag(account, path, this.tagService.addTag(tagForm));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public @ResponseBody ResponseEntity removeTag(@CurrentUserAccount Account account, @PathVariable String path
            , @RequestBody TagForm tagForm) {
        Optional<Tag> tag = this.tagService.findByTitle(tagForm);
        if(tag.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        this.studyService.removeTag(account, path, tag.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String zonesSettingForm(@CurrentUserAccount Account account, @PathVariable String path
            , Model model) throws JsonProcessingException {
        model.addAttribute("account", account);

        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        model.addAttribute("zones", study.getZones().stream().map(Zone::toString));

        List<String> allZones = this.zoneService.findAllZones().stream().map(Zone::toString)
                .collect(Collectors.toList());
        model.addAttribute("whitelist", this.objectMapper.writeValueAsString(allZones));
        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    public @ResponseBody ResponseEntity addZone(@CurrentUserAccount Account account, @PathVariable String path
            , @RequestBody ZoneForm zoneForm) {
        Optional<Zone> zone = this.zoneService.findByZoneForm(zoneForm);
        if(zone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        this.studyService.addZone(account, path, zone.get());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    public @ResponseBody ResponseEntity removeZone(@CurrentUserAccount Account account, @PathVariable String path
            , @RequestBody ZoneForm zoneForm) {
        Optional<Zone> zone = this.zoneService.findByZoneForm(zoneForm);
        if(zone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        this.studyService.removeZone(account, path, zone.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute("account", account);
        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.publish(account, path);
        attributes.addFlashAttribute("message", "스터디를 공개하였습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.close(account, path);
        attributes.addFlashAttribute("message", "스터디를 시작하였습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.startRecruit(account, path);
        attributes.addFlashAttribute("message", "구성원 모집을 시작하였습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.stopRecruit(account, path);
        attributes.addFlashAttribute("message", "구성원 모집을 중지하였습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/study/path")
    public String changeStudyPath(@CurrentUserAccount Account account, @PathVariable String path
            , @RequestParam String newPath, RedirectAttributes attributes) {
        this.studyService.changeStudyPath(account, path, newPath);
        attributes.addFlashAttribute("message", "스터디 경로를 변경하였습니다.");
        return "redirect:/study/" + newPath + "/settings/study";
    }

    @PostMapping("/study/title")
    public String changeStudyTitle(@CurrentUserAccount Account account, @PathVariable String path
            , @RequestParam String newTitle, RedirectAttributes attributes) {
        this.studyService.changeStudyTitle(account, path, newTitle);
        attributes.addFlashAttribute("message", "스터디명을 변경하였습니다.");
        return "redirect:/study/" + path + "/settings/study";
    }

    @PostMapping("/study/remove")
    public String changeStudyTitle(@CurrentUserAccount Account account, @PathVariable String path
            , RedirectAttributes attributes) {
        this.studyService.removeStudy(account, path);
        attributes.addFlashAttribute("message", "스터디를 완전 삭제하였습니다.");
        return "redirect:/";
    }

}
