package studyolle.event.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.event.application.EventService;
import studyolle.event.dto.EventForm;
import studyolle.event.dto.EventFormValidator;
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
    private final EventFormValidator eventFormValidator;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(this.eventFormValidator);
    }

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
        Study study = this.studyService.findByPath(path);
        // TODO 오류를 발생시키지 않고, 이전 페이지로 돌아가고 툴팁을 통해 알려주도록 변경
        study.checkedManager(account);
        model.addAttribute("study", study);
        model.addAttribute("eventForm", new EventForm());
        return "event/form";
    }

    /**
     * 모임 생성
     * @param account 
     * @param path
     * @param eventForm
     * @param errors
     * @param model
     * @return
     */
    @PostMapping("/study/{path}/new-event")
    public String createEvent(@CurrentUserAccount Account account, @PathVariable String path
            , @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = this.studyService.findByPath(path);
        study.checkedManager(account);

        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            model.addAttribute("study", study);
            return "event/form";
        }

        this.eventService.createEvent(study, account, eventForm);
        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8);
    }
}
