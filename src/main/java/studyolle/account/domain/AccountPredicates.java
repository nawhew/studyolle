package studyolle.account.domain;

import com.querydsl.core.types.Predicate;
import studyolle.tag.domain.Tag;
import studyolle.zone.domain.Zone;

import java.util.Set;

public class AccountPredicates {

    /**
     * 해당 관심분야와 활동지역을 가지고 있는 계정을 조회합니다.
     */
    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        return account
                .zones.any().in(zones)
                .and(account.tags.any().in(tags));
    }
}
