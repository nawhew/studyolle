package studyolle.event.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.enrollment.domain.Enrollment;
import studyolle.enrollment.domain.EnrollmentRepository;
import studyolle.event.domain.Event;
import studyolle.event.domain.EventRepository;
import studyolle.event.dto.EventForm;
import studyolle.study.domain.Study;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

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

    public void deleteEvent(Long id) {
        Event event = this.eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 모임이 없습니다."));
        this.eventRepository.delete(event);
    }

    /**
     * 새로운 등록을 모임에 추가합니다.
     * @param account
     * @param id
     */
    public void addNewEnrollment(Account account, Long id) {
        Event event = this.findWithEnrollmentsById(id);
        Enrollment enrollment = this.createEnrollment(event, account);
        event.addEnrollment(enrollment);

    }

    /**
     * 새로운 등록을 생성합니다.
     * 요청한 모임과 계정 정보로 이미 생성된 등록이 있는지 중복체크 합니다.
     * 중복되는 등록이 있는경우 오류를 발생합니다.
     * 받은 계정정보와 현재시간을 등록시간으로 초기화 하며,
     * 모임의 종류에 따라 승인여부를 초기화해줍니다. (선착순 모임이며 남은 자리가 있는 경우 바로 승인)
     * @param event
     * @param account
     * @return
     */
    private Enrollment createEnrollment(Event event, Account account) {
        if(this.enrollmentRepository.existsByEventAndAccount(event, account)) {
            throw new IllegalArgumentException("이미 모임에 등록되어 있습니다.");
        }

        Enrollment enrollment = Enrollment.builder()
                .account(account)
                .accepted(event.isImmediatelyAcceptEnrollment())
                .enrolledAt(LocalDateTime.now())
                .build();
        return this.enrollmentRepository.save(enrollment);
    }
}
