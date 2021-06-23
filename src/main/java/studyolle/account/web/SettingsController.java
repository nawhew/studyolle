package studyolle.account.web;

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
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.account.dto.*;
import studyolle.tag.application.TagService;
import studyolle.tag.domain.Tag;
import studyolle.zone.application.ZoneService;
import studyolle.zone.domain.Zone;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    protected static final String VIEW_SETTINGS_PROFILE = "settings/profile";
    protected static final String URL_SETTINGS_PROFILE = "/" + VIEW_SETTINGS_PROFILE;
    protected static final String VIEW_SETTINGS_PASSWORD = "settings/password";
    protected static final String URL_SETTINGS_PASSWORD = "/" + VIEW_SETTINGS_PASSWORD;
    protected static final String VIEW_SETTINGS_NOTIFICATIONS = "settings/notifications";
    protected static final String URL_SETTINGS_NOTIFICATIONS = "/" + VIEW_SETTINGS_NOTIFICATIONS;
    protected static final String VIEW_SETTINGS_ACCOUNT = "settings/account";
    protected static final String URL_SETTINGS_ACCOUNT = "/" + VIEW_SETTINGS_ACCOUNT;
    protected static final String VIEW_SETTINGS_TAGS = "settings/tags";
    protected static final String URL_SETTINGS_TAGS = "/" + VIEW_SETTINGS_TAGS;
    protected static final String VIEW_SETTINGS_ZONES = "settings/zones";
    protected static final String URL_SETTINGS_ZONES = "/" + VIEW_SETTINGS_ZONES;

    private final AccountService accountService;
    private final NicknameFormValidator nicknameFormValidator;
    private final TagService tagService;
    private final ObjectMapper objectMapper;
    private final ZoneService zoneService;

    @InitBinder("passwordForm")
    public void initBinderByPasswordForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void initBinderByNicknameForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }

    @GetMapping(URL_SETTINGS_PROFILE)
    public String profileSettingForm(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("profile", Profile.of(account));
        return VIEW_SETTINGS_PROFILE;
    }

    @PostMapping(URL_SETTINGS_PROFILE)
    public String updateProfile(@CurrentUserAccount Account account, @Valid Profile profile
            , Errors errors, Model model, RedirectAttributes attributes) {

        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            return VIEW_SETTINGS_PROFILE;
        }

        this.accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:" + URL_SETTINGS_PROFILE;
    }

    @GetMapping(URL_SETTINGS_PASSWORD)
    public String passwordSettingForm(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("passwordForm", new PasswordForm());
        return VIEW_SETTINGS_PASSWORD;
    }

    @PostMapping(URL_SETTINGS_PASSWORD)
    public String updatePassword(@CurrentUserAccount Account account, @Valid PasswordForm passwordForm
            , Errors errors, Model model, RedirectAttributes attributes) {

        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            return VIEW_SETTINGS_PASSWORD;
        }

        this.accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:" + URL_SETTINGS_PASSWORD;
    }

    @GetMapping(URL_SETTINGS_NOTIFICATIONS)
    public String notificationsSettingForm(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("notifications", Notifications.of(account));
        return VIEW_SETTINGS_NOTIFICATIONS;
    }

    @PostMapping(URL_SETTINGS_NOTIFICATIONS)
    public String updateNotifications(@CurrentUserAccount Account account, @Valid Notifications notifications
            , Errors errors, Model model, RedirectAttributes attributes) {

        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            return VIEW_SETTINGS_NOTIFICATIONS;
        }

        this.accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림설정을 변경했습니다.");
        return "redirect:" + URL_SETTINGS_NOTIFICATIONS;
    }

    @GetMapping(URL_SETTINGS_ACCOUNT)
    public String accountSettingForm(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("nicknameForm", NicknameForm.of(account));
        return VIEW_SETTINGS_ACCOUNT;
    }

    @PostMapping(URL_SETTINGS_ACCOUNT)
    public String updateAccount(@CurrentUserAccount Account account, @Valid NicknameForm nicknameForm
            , Errors errors, Model model, RedirectAttributes attributes) {

        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            return VIEW_SETTINGS_ACCOUNT;
        }

        this.accountService.updateNickname(account, nicknameForm);
        attributes.addFlashAttribute("message", "닉네임설정을 변경했습니다.");
        return "redirect:" + URL_SETTINGS_ACCOUNT;
    }

    @GetMapping(URL_SETTINGS_TAGS)
    public String tagsSettingForm(@CurrentUserAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute("account", account);
        model.addAttribute("tags", this.accountService.findTags(account).stream().map(Tag::getTitle));

        List<String> allTags = this.tagService.findAllTags().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", this.objectMapper.writeValueAsString(allTags));
        return VIEW_SETTINGS_TAGS;
    }

    @PostMapping(URL_SETTINGS_TAGS + "/add")
    public @ResponseBody ResponseEntity addTag(@CurrentUserAccount Account account, @RequestBody TagForm tagForm) {
        this.accountService.addTag(account, this.tagService.addTag(tagForm));
        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_SETTINGS_TAGS + "/remove")
    public @ResponseBody ResponseEntity removeTag(@CurrentUserAccount Account account, @RequestBody TagForm tagForm) {
        Optional<Tag> tag = this.tagService.findByTitle(tagForm);
        if(tag.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        this.accountService.removeTag(account, tag.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping(URL_SETTINGS_ZONES)
    public String zonesSettingForm(@CurrentUserAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute("account", account);
        model.addAttribute("zones", this.accountService.findZones(account).stream().map(Zone::toString));

        List<String> allZones = this.zoneService.findAllZones().stream().map(Zone::toString)
                .collect(Collectors.toList());
        model.addAttribute("whitelist", this.objectMapper.writeValueAsString(allZones));
        return VIEW_SETTINGS_ZONES;
    }

    @PostMapping(URL_SETTINGS_ZONES + "/add")
    public @ResponseBody ResponseEntity addZone(@CurrentUserAccount Account account, @RequestBody ZoneForm zoneForm) {
        Optional<Zone> zone = this.zoneService.findByZoneForm(zoneForm);
        if(zone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        this.accountService.addZone(account, zone.get());
        return ResponseEntity.ok().build();
    }

    @PostMapping(URL_SETTINGS_ZONES + "/remove")
    public @ResponseBody ResponseEntity removeZone(@CurrentUserAccount Account account, @RequestBody ZoneForm zoneForm) {
        Optional<Zone> zone = this.zoneService.findByZoneForm(zoneForm);
        if(zone.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        this.accountService.removeZone(account, zone.get());
        return ResponseEntity.ok().build();
    }
}
