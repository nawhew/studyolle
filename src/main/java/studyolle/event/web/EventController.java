package studyolle.event.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.enrollment.domain.Enrollment;
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

    /**
     * 모임 상세 화면 요청 처리
     * @param account
     * @param path
     * @param id
     * @param model
     * @return
     */
    @GetMapping("/study/{path}/events/{id}")
    public String eventsView(@CurrentUserAccount Account account, @PathVariable String path, @PathVariable Long id
            , Model model) {
        model.addAttribute("account", account);
        model.addAttribute("study", this.studyService.findByPath(path));
        model.addAttribute("event", this.eventService.findById(id));
        return "event/view";
    }

    /**
     * 스터디의 모임들을 모두 보여주는 화면 요청 처리
     * @param account
     * @param path
     * @param model
     * @return
     */
    @GetMapping("/study/{path}/events")
    public String eventsView(@CurrentUserAccount Account account, @PathVariable String path, Model model) {
        model.addAttribute("account", account);
        Study study = this.studyService.findByPath(path);
        model.addAttribute("study", study);
        this.addAttributeNewEventsAndOldEventsByStudy(model, study);
        return "study/events";
    }

    /**
     * Model attribute에 신규모임들과 지난모임들 추가
     * @param model
     * @param study
     */
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

    @GetMapping("/study/{path}/events/{id}/edit")
    public String updateEventForm(@CurrentUserAccount Account account, @PathVariable String path
            , @PathVariable Long id, Model model) {
        model.addAttribute("account", account);

        Study study = this.studyService.findByPath(path);
        study.checkedManager(account);
        model.addAttribute("study", study);

        Event event = this.eventService.findById(id);
        model.addAttribute("event", event);
        model.addAttribute("eventForm", EventForm.of(event));
        return "event/update-form";
    }

    @PostMapping("/study/{path}/events/{id}/edit")
    public String updateEvent(@CurrentUserAccount Account account, @PathVariable String path, @PathVariable Long id
            , @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = this.studyService.findByPath(path);
        study.checkedManager(account);
        Event event = this.eventService.updateEvent(id, eventForm, study);

        if(errors.hasErrors()) {
            model.addAttribute("account", account);
            model.addAttribute("study", study);
            model.addAttribute("event", event);
            return "event/update-form";
        }

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + id;
    }

    /**
     * 해당 모임을 취소합니다. (DB에서 삭제됨)
     * @param account
     * @param path
     * @param id
     * @return
     */
    @DeleteMapping("/study/{path}/events/{id}")
    public String cancelEvent(@CurrentUserAccount Account account, @PathVariable String path, @PathVariable Long id) {
        Study study = this.studyService.findStudyWithManagersByPath(path);
        study.checkedManager(account);
        this.eventService.deleteEvent(id, study);
        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events";
    }

    @PostMapping("/study/{path}/events/{id}/enroll")
    public String enrollEvent(@CurrentUserAccount Account account, @PathVariable String path, @PathVariable Long id) {
        this.studyService.checkedMember(path, account);
        this.eventService.addNewEnrollment(account, id);

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + id;
    }


    @PostMapping("/study/{path}/events/{id}/leave")
    public String cancelEnrollEvent(@CurrentUserAccount Account account, @PathVariable String path, @PathVariable Long id) {
        this.studyService.checkedMember(path, account);
        this.eventService.cancelEnrollment(account, id);

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + id;
    }


    @GetMapping("/study/{path}/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUserAccount Account account, @PathVariable String path
            , @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        this.studyService.checkedManager(path, account);
        this.eventService.acceptEnrollment(eventId, enrollmentId);

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + eventId;
    }

    @GetMapping("/study/{path}/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUserAccount Account account, @PathVariable String path
            , @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        this.studyService.checkedManager(path, account);
        this.eventService.rejectEnrollment(eventId, enrollmentId);

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + eventId;
    }

    @GetMapping("/study/{path}/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUserAccount Account account, @PathVariable String path
            , @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        this.studyService.checkedManager(path, account);
        this.eventService.checkInEnrollment(eventId, enrollmentId);

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + eventId;
    }

    @GetMapping("/study/{path}/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentUserAccount Account account, @PathVariable String path
            , @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        this.studyService.checkedManager(path, account);
        this.eventService.cancelCheckInEnrollment(eventId, enrollmentId);

        return "redirect:/study/" + URLEncoder.encode(path, StandardCharsets.UTF_8) + "/events/" + eventId;
    }
}
