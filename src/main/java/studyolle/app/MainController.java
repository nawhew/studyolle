package studyolle.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.study.application.StudyService;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyService studyService;

    @GetMapping("/")
    public String index(@CurrentUserAccount Account account, Model model) {
        if(account != null) {
            model.addAttribute("account", account);
        }
        model.addAttribute("studyList", this.studyService.findTop9ByOrderPublishedDateTime());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "/login";
    }
}
