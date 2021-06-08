package studyolle.settings.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.settings.dto.*;

import javax.validation.Valid;

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

    private final AccountService accountService;
    private final NicknameFormValidator nicknameFormValidator;

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
}
