package studyolle.settings.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.settings.dto.Profile;

import javax.validation.Valid;

@Controller
public class SettingsController {

    protected static final String VIEW_SETTINGS_PROFILE = "settings/profile";
    protected static final String URL_SETTINGS_PROFILE = "/settings/profile";

    private final AccountService accountService;

    public SettingsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping(URL_SETTINGS_PROFILE)
    public String profileSetting(@CurrentUserAccount Account account, Model model) {
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
}
