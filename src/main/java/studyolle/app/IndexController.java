package studyolle.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.account.domain.Account;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(@CurrentUserAccount Account account, Model model) {
        if(account != null) {
            model.addAttribute(account);
        }
        return "index";
    }
}
