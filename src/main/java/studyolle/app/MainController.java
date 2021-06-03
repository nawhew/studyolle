package studyolle.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(@CurrentUserAccount Account account, Model model) {
        if(account != null) {
            model.addAttribute(account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "/login";
    }
}
