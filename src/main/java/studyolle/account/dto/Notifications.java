package studyolle.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import studyolle.account.domain.Account;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class Notifications {

    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyEnrollmentResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    public static Notifications of(Account account) {
        return Notifications.builder()
                .studyCreatedByEmail(account.isStudyCreatedByEmail())
                .studyCreatedByWeb(account.isStudyCreatedByWeb())
                .studyEnrollmentResultByEmail(account.isStudyEnrollmentResultByEmail())
                .studyEnrollmentResultByWeb(account.isStudyEnrollmentResultByWeb())
                .studyUpdatedByEmail(account.isStudyUpdatedByEmail())
                .studyUpdatedByWeb(account.isStudyUpdatedByWeb())
                .build();
    }
}
