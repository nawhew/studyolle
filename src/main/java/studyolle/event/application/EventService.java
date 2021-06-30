package studyolle.event.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.enrollment.application.EnrollmentEvent;
import studyolle.enrollment.domain.Enrollment;
import studyolle.enrollment.domain.EnrollmentRepository;
import studyolle.event.domain.Event;
import studyolle.event.domain.EventRepository;
import studyolle.event.dto.EventForm;
import studyolle.study.application.StudyUpdatedEvent;
import studyolle.study.domain.Study;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void createEvent(Study study, Account account, EventForm eventForm) {
        Event event = eventForm.toEntity();
        event.init(study, account);
        this.eventRepository.save(event);
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study
                , "새로운 모임(" + event.getTitle() + ")이 추가되었습니다."));
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
     * @param study
     * @return
     */
    public Event updateEvent(Long id, EventForm eventForm, Study study) {
        Event event = this.findWithEnrollmentsById(id);
        if(this.canChangeLimitOfEnrollments(eventForm, event)) {
            event.updateByForm(eventForm);
            event.acceptWaitingEnrollment();
        }
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study
                , event.getTitle() + "모임 내용이 수정되었습니다."));
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

    public void deleteEvent(Long id, Study study) {
        Event event = this.eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID를 가진 모임이 없습니다."));
        this.eventRepository.delete(event);
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study
                , event.getTitle() + " 모임이 삭제되었습니다."));
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

    public void cancelEnrollment(Account account, Long id) {
        Event event = this.deleteEnrollment(account, id);
        event.acceptWaitingEnrollment();
    }

    /**
     * 모임의 등록을 취소합니다.
     * @param account
     * @param id
     */
    private Event deleteEnrollment(Account account, Long id) {
        Event event = this.findWithEnrollmentsById(id);
        Enrollment enrollment = this.enrollmentRepository.findByEventAndAccount(event, account)
                .orElseThrow(() -> new IllegalArgumentException("모임에 등록되어 있지 않습니다."));
        event.deleteEnrollment(enrollment);
        this.enrollmentRepository.delete(enrollment);
        return event;
    }

    /**
     * 해당 등록의 참석을 승인합니다.
     * @param eventId
     * @param enrollmentId
     */
    public void acceptEnrollment(Long eventId, Long enrollmentId) {
        Event event = this.findWithEnrollmentsById(eventId);
        Enrollment persistEnrollment = event.getEnrollments().stream()
                .filter(enrollment -> enrollment.getId().equals(enrollmentId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("모임에 등록되어 있지 않습니다."));
        if(!event.canAccept(persistEnrollment)) {
            throw new IllegalArgumentException("승인 할 수 없는 등록정보 입니다.");
        }
        persistEnrollment.accept();
        this.eventPublisher.publishEvent(new EnrollmentEvent(persistEnrollment
                , event.getTitle() + " 모임에 참가 승인되었습니다."));
    }

    /**
     * 해당 등록의 참석을 거부(취소)합니다.
     * @param eventId
     * @param enrollmentId
     */
    public void rejectEnrollment(Long eventId, Long enrollmentId) {
        Event event = this.findWithEnrollmentsById(eventId);
        Enrollment enrollment = event.findEnrollment(enrollmentId);
        if(!event.canReject(enrollment)) {
            throw new IllegalArgumentException("취소 할 수 없는 등록정보 입니다.");
        }
        enrollment.reject();
        this.eventPublisher.publishEvent(new EnrollmentEvent(enrollment
                , event.getTitle() + " 모임에 참가 거부되었습니다."));
    }

    /**
     * 출석체크합니다.
     * @param eventId
     * @param enrollmentId
     */
    public void checkInEnrollment(Long eventId, Long enrollmentId) {
        Event event = this.findWithEnrollmentsById(eventId);
        Enrollment enrollment = event.findEnrollment(enrollmentId);
        enrollment.checkIn();
    }

    /**
     * 출석체크를 취소 합니다.
     * @param eventId
     * @param enrollmentId
     */
    public void cancelCheckInEnrollment(Long eventId, Long enrollmentId) {
        Event event = this.findWithEnrollmentsById(eventId);
        Enrollment enrollment = event.findEnrollment(enrollmentId);
        enrollment.cancelCheckIn();
    }

    /**
     * 참석 할 모임을 찾습니다.
     * @param account
     * @return
     */
    public List<Enrollment> findAcceptedEnrollmentsByAccount(Account account) {
        return this.enrollmentRepository
                .findWithEventByAccountAndAcceptedAndAttended(account, true, false);
    }
}
