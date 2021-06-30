package studyolle.app;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import studyolle.account.application.AccountService;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.event.application.EventService;
import studyolle.study.application.StudyService;
import studyolle.study.domain.Study;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final AccountService accountService;
    private final StudyService studyService;
    private final EventService eventService;

    @GetMapping("/")
    public String index(@CurrentUserAccount Account account, Model model) {
        if(account != null) {
            this.addAttributeExistAccount(account, model);
            return "index-after-login";
        }
        model.addAttribute("studyList", this.studyService.findTop9ByOrderPublishedDateTime());
        return "index";
    }

    /**
     * 로그인 된 계정의 스터디 및 모임 정보를 추가합니다.
     * @param account
     * @param model
     */
    private void addAttributeExistAccount(Account account, Model model) {
        Account persistAccount = this.accountService.findWithTagsAndZonesById(account);
        model.addAttribute("account", persistAccount);
        model.addAttribute("enrollmentList", this.eventService.findAcceptedEnrollmentsByAccount(persistAccount));
        model.addAttribute("studyList", this.studyService.findByZonesAndTagsForAccount(persistAccount));
        model.addAttribute("studyManagerOf", this.studyService.findManagedStudy(persistAccount));
        model.addAttribute("studyMemberOf", this.studyService.findJoinedStudy(persistAccount));
    }

    @GetMapping("/login")
    public String login() {
        return "/login";
    }
}
