package studyolle.settings.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.settings.dto.Profile;

@Controller
public class SettingsController {

    @GetMapping("/settings/profile")
    public String profileSetting(@CurrentUserAccount Account account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("profile", Profile.of(account));
        return "settings/profile";
    }
}
