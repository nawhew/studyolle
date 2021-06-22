package studyolle.enrollment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.event.domain.Event;

@Transactional
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Transactional(readOnly = true)
    boolean existsByEventAndAccount(Event event, Account account);
}
