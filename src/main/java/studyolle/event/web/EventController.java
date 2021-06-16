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
import studyolle.event.domain.Event;
import studyolle.event.dto.EventForm;
import studyolle.event.dto.EventFormValidator;
import studyolle.study.application.StudyService;
import studyolle.study.domain.Study;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/study/{path}/events/{id}")
    public String eventsView(@CurrentUserAccount Account account, @PathVariable String path, @PathVariable Long id
            , Model model) {
        model.addAttribute("account", account);
        model.addAttribute("study", this.studyService.findByPath(path));
        model.addAttribute("event", this.eventService.findById(id));
        return "event/view";
    }

    @GetMapping("/study/{path}/events")
    public String eventsView(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute("account", account);
        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        this.addAttributeNewEventsAndOldEventsByStudy(model, study);
        return "study/events";
    }

    private void addAttributeNewEventsAndOldEventsByStudy(Model model, Study study) {
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        this.eventService.findWithEnrollmentsByStudy(study).stream()
            .forEach(event -> {
                if(event.getEndDateTime().isBefore(LocalDateTime.now())) {
                    oldEvents.add(event);
                } else {
                    newEvents.add(event);
                }
            });
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
    }
}
