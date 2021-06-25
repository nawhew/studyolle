package studyolle.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.notification.domain.Notification;
import studyolle.notification.domain.NotificationRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    public List<Notification> findByAccountAndCheckedOrderByCreatedDateTimeDesc(Account account, boolean checked) {
        return this.notificationRepository
                    .findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, checked);
    }

    /**
     * 해당 계정의 읽거나 읽지 않은 알림 수를 가져옵니다.
     * @param account
     * @param checked
     * @return
     */
    public long countByAccountAndChecked(Account account, boolean checked) {
        return this.notificationRepository.countByAccountAndChecked(account, checked);
    }

    public void markAsRead(List<Notification> notifications) {
        for(Notification notification : notifications) {
            notification.checkAsRead();
        }
    }

    /**
     * 해당 계정에서 읽은 알림을 삭제합니다
     * @param account 
     * @param checked
     */
    public void deleteByAccountAndChecked(Account account, boolean checked) {
        this.notificationRepository.deleteByAccountAndChecked(account, checked);
    }
}
