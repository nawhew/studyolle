package studyolle.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;

@Transactional
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Transactional(readOnly = true)
    long countByAccountAndChecked(Account account, boolean checked);
}
