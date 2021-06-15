package studyolle.study.domain;

import lombok.*;
import studyolle.account.domain.Account;
import studyolle.account.domain.security.UserAccount;
import studyolle.study.dto.StudyDescriptionForm;
import studyolle.tag.domain.Tag;
import studyolle.zone.domain.Zone;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
@NamedEntityGraph(name = "Study.withAllRelations", attributeNodes = {@NamedAttributeNode("tags")
        , @NamedAttributeNode("zones"), @NamedAttributeNode("managers"), @NamedAttributeNode("members")})
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
        @NamedAttributeNode("tags"), @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"), @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.noRelations", attributeNodes = {})
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

    /**
     * 배너 이미지를 반환합니다. (없는 경우 기본 이미지를 반환합니다)
     * @return 
     */
    public String getImage() {
        return image != null ? image : "/images/default-banner.png";
    }

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
        return this.isMember(userAccount.getAccount());
    }

    public boolean isMember(Account account) {
        return this.members.contains(account);
    }

    public boolean isManager(UserAccount userAccount) {
        return this.isManager(userAccount.getAccount());
    }

    public boolean isManager(Account account) {
        return this.managers.contains(account);
    }

    /**
     * 해당 계정이 매니저가 아닌 경우 오류를 던집니다
     * @param account 
     */
    public void checkedManager(Account account) {
        if(!this.isManager(account)) {
            throw new IllegalArgumentException("매니저만 스터디 정보를 수정 할 수 있습니다.");
        }
    }

    public void updateDescription(Account account, StudyDescriptionForm studyDescriptionForm) {
        this.checkedManager(account);
        this.shortDescription = studyDescriptionForm.getShortDescription();
        this.fullDescription = studyDescriptionForm.getFullDescription();
    }

    public void updateImage(Account account, String image) {
        this.checkedManager(account);
        this.image = image;
    }

    public void updateUseBanner(Account account, boolean useBanner) {
        this.checkedManager(account);
        this.useBanner = useBanner;
    }

    public Set<String> getTagTitles() {
        return this.tags.stream().map(Tag::getTitle).collect(Collectors.toSet());
    }

    public void addTag(Account account, Tag tag) {
        this.checkedManager(account);
        this.tags.add(tag);
    }

    public void removeTag(Account account, Tag tag) {
        this.checkedManager(account);
        this.tags.remove(tag);
    }

    public void addZone(Account account, Zone zone) {
        this.checkedManager(account);
        this.zones.add(zone);
    }

    public void removeZone(Account account, Zone zone) {
        this.checkedManager(account);
        this.zones.remove(zone);
    }

    /**
     * draft 상태의 스터디를 공개합니다.
     * @param account
     */
    public void publish(Account account) {
        this.checkedManager(account);

        if(this.isDraft()) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }
    }

    private boolean isDraft() {
        if(!this.published && !this.closed) {
            return true;
        }
        throw new IllegalArgumentException("이미 공개되었거나 종료 된 스터디입니다.");
    }

    public void close(Account account) {
        this.checkedManager(account);

        if(this.isOpened()) {
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }
    }

    private boolean isOpened() {
        if(this.published && !this.closed) {
            return true;
        }
        throw new IllegalArgumentException("시작 할 수 없는 스터디입니다.");
    }

    public void startRecruit(Account account) {
        this.checkedManager(account);

        if(this.isOpened()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }
    }

    public void stopRecruit(Account account) {
        this.checkedManager(account);

        if(this.recruiting && this.isOpened()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }
    }

    public void changePath(Account account, String newPath) {
        this.checkedManager(account);
        this.path = newPath;
    }

    public void changeTitle(Account account, String newTitle) {
        this.checkedManager(account);
        if(newTitle.length() > 50) {
            throw new IllegalArgumentException("스터디 이름은 50자까지 가능합니다.");
        }
        this.title = newTitle;
    }

    public void joinMember(Account account) {
        this.members.add(account);
    }

    public void leaveMember(Account account) {
        this.members.remove(account);
    }
}
