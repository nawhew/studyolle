package studyolle.event.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.event.domain.Event;
import studyolle.event.domain.EventRepository;
import studyolle.event.dto.EventForm;
import studyolle.study.domain.Study;

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
}
