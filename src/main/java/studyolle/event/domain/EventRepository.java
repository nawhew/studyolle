package studyolle.event.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import studyolle.study.domain.Study;

import java.util.List;
import java.util.Optional;

@Transactional
public interface EventRepository extends JpaRepository<Event, Long> {

    @Transactional(readOnly = true)
    @EntityGraph(value = "Event.withEnrollments", type = EntityGraph.EntityGraphType.LOAD)
    List<Event> findWithEnrollmentsByStudyOrderByStartDateTime(Study study);

    @EntityGraph(value = "Event.withEnrollments", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Event> findWithEnrollmentsById(Long id);
}
