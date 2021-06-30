package studyolle.enrollment.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.event.domain.Event;

import java.util.List;
import java.util.Optional;

@Transactional
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Transactional(readOnly = true)
    boolean existsByEventAndAccount(Event event, Account account);

    Optional<Enrollment> findByEventAndAccount(Event event, Account account);

    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = {"event"})
    List<Enrollment> findWithEventByAccountAndAcceptedAndAttended(Account account, boolean accepted, boolean attended);
}
