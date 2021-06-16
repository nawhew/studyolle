package studyolle.event.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.event.domain.Event;
import studyolle.event.domain.EventRepository;
import studyolle.event.dto.EventForm;
import studyolle.study.domain.Study;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public void createEvent(Study study, Account account, EventForm eventForm) {
        Event event = eventForm.toEntity();
        event.init(study, account);
        this.eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return this.eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 모임이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<Event> findWithEnrollmentsByStudy(Study study) {
        return this.eventRepository.findWithEnrollmentsByStudyOrderByStartDateTime(study);
    }

    public Event findWithEnrollmentsById(Long id) {
        return this.eventRepository.findWithEnrollmentsById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 모임이 없습니다."));
    }

    /**
     * 입력받은 폼으로 모임의 값을 수정합니다.
     * @param id
     * @param eventForm
     * @return
     */
    public Event updateEvent(Long id, EventForm eventForm) {
        Event event = this.findWithEnrollmentsById(id);
        if(canChangeLimitOfEnrollments(eventForm, event)) {
            event.updateByForm(eventForm);
        }
        return event;
    }

    /**
     * 모집인원을 변경 할 수 있는지 여부 반환.
     * 변경을 원하는 모집인원이 현재까지 확정된 참가 신청 수보다 크거나 같은 경우.
     * @param eventForm
     * @param event
     * @return
     */
    private boolean canChangeLimitOfEnrollments(EventForm eventForm, Event event) {
        return event.getEnrollments().size() <= eventForm.getLimitOfEnrollments();
    }
}
