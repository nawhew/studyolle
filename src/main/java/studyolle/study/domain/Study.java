package studyolle.study.domain;

import lombok.*;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.UserAccount;
import studyolle.tag.domain.Tag;
import studyolle.zone.domain.Zone;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
@NamedEntityGraph(name = "Study.withAllRelations", attributeNodes = {@NamedAttributeNode("tags")
        , @NamedAttributeNode("zones"), @NamedAttributeNode("managers"), @NamedAttributeNode("members")})
public class Study {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    private final Set<Account> managers = new HashSet<>();

    @ManyToMany
    private final Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private final Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private final Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public Study addCreateMember(Account account) {
        this.managers.add(account);
        this.members.add(account);
        return this;
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);

    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }
}