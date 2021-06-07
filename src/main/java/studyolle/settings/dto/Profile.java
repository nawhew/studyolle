package studyolle.settings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import studyolle.account.domain.Account;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class Profile {
    private String bio;
    private String url;
    private String occupation;
    private String location;

    public static Profile of(Account account) {
        return Profile.builder()
                .bio(account.getBio())
                .url(account.getUrl())
                .occupation(account.getOccupation())
                .location(account.getLocation())
                .build();
    }
}