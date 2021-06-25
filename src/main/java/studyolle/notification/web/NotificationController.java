package studyolle.notification.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.CurrentUserAccount;
import studyolle.notification.application.NotificationService;
import studyolle.notification.domain.Notification;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String getNotifications(@CurrentUserAccount Account account, Model model) {
        List<Notification> notifications = this.notificationService
                .findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, false);
        long numberOfChecked = this.notificationService.countByAccountAndChecked(account, true);
        this.putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);

        this.notificationService.markAsRead(notifications);

        return "notification/list";
    }

    /**
     * Model에 전체 알림 목록, 새로운 스터디 알림 목록, 새로운 모임 등록 알림 목록, 스터디 알림 목록
     * , 읽은 알림 수, 읽지않은 알림 수를 넣어줍니다.
     * @param model
     * @param notifications
     * @param numberOfChecked
     * @param numberOfNotChecked
     */
    private void putCategorizedNotifications(Model model, List<Notification> notifications
            ,long numberOfChecked, long numberOfNotChecked) {
        List<Notification> newStudyNotifications = new ArrayList<>();
        List<Notification> eventEnrollmentNotifications = new ArrayList<>();
        List<Notification> watchingStudyNotifications = new ArrayList<>();
        for (var notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED: newStudyNotifications.add(notification); break;
                case EVENT_ENROLLMENT: eventEnrollmentNotifications.add(notification); break;
                case STUDY_UPDATED: watchingStudyNotifications.add(notification); break;
            }
        }

        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }

    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentUserAccount Account account, Model model) {
        List<Notification> notifications = this.notificationService
                .findByAccountAndCheckedOrderByCreatedDateTimeDesc(account, true);
        long numberOfNotChecked = this.notificationService.countByAccountAndChecked(account, false);
        this.putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentUserAccount Account account) {
        this.notificationService.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }
}
