package studyolle.event.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.event.application.EventService;
import studyolle.event.dto.EventForm;
import studyolle.study.application.StudyService;
import studyolle.study.domain.Study;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;

    /**
     * 신규 모임 생성 폼 요청 처리
     * @param account
     * @param path
     * @param model
     * @return
     */
    @GetMapping("/study/{path}/new-event")
    public String createEventForm(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("study", this.studyService.findByPath(path));
        model.addAttribute("eventForm", new EventForm());
        return "event/form";
    }

    @PostMapping("/study/{path}/new-event")
    public String createEvent(@CurrentUserAccount Account account, @PathVariable String path
            , @Valid EventForm eventForm) {
        Study study = this.studyService.findByPath(path);
        this.eventService.createEvent(study, account, eventForm);
        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
