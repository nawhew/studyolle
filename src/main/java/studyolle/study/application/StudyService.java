package studyolle.study.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.study.domain.Study;
import studyolle.study.domain.StudyRepository;
import studyolle.study.dto.StudyDescriptionForm;
import studyolle.study.dto.StudyForm;
import studyolle.tag.domain.Tag;
import studyolle.zone.domain.Zone;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountRepository accountRepository;

    /**
     * 입력받은 폼으로 새로운 스터디를 개설합니다.
     * 생성한 계정을 관리자, 멤버로 추가합니다.
     * @param account
     * @param studyForm
     */
    public Study createStudy(Account account, StudyForm studyForm) {
        Study persistStudy = this.studyRepository.save(studyForm.toEntity().addCreateMember(account));
        return persistStudy;
    }

    public Study findByPath(String path) {
        return this.studyRepository.findByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
    }

    public Study findStudyWithManagersByPath(String path) {
        return this.studyRepository.findStudyWithManagersByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
    }

    public Study findStudyWithMembersByPath(String path) {
        return this.studyRepository.findStudyWithMembersByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
    }

    public Study findAccountWithTagsByPath(String path) {
        return this.studyRepository.findStudyWithTagsByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
    }

    public Study findAccountWithZonesByPath(String path) {
        return this.studyRepository.findStudyWithZonesByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
    }

    public Study updateStudyDescription(Account account, String path, StudyDescriptionForm studyDescriptionForm) {
        Study study = this.findByPath(path);
        study.updateDescription(account, studyDescriptionForm);
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study, "스터디 소개가 수정되었습니다."));
        return study;
    }

    public void updateStudyBannerImage(Account account, String path, String image) {
        Study study = this.findByPath(path);
        study.updateImage(account, image);
    }

    public void updateStudyUseBanner(Account account, String path, boolean useBanner) {
        Study study = this.findByPath(path);
        study.updateUseBanner(account, useBanner);
    }

    public void addTag(Account account, String path, Tag tag) {
        Study study = this.findAccountWithTagsByPath(path);
        study.addTag(account, tag);
    }

    public void removeTag(Account account, String path, Tag tag) {
        Study study = this.findAccountWithTagsByPath(path);
        study.removeTag(account, tag);
    }

    public void addZone(Account account, String path, Zone zone) {
        Study study = this.findAccountWithZonesByPath(path);
        study.addZone(account, zone);
    }

    public void removeZone(Account account, String path, Zone zone) {
        Study study = this.findAccountWithZonesByPath(path);
        study.removeZone(account, zone);
    }

    /**
     * 스터디를 공개합니다.
     * 스터디를 공개하면 검색 가능해집니다.
     * @param account
     * @param path
     */
    public void publish(Account account, String path) {
        Study study = this.findByPath(path);
        study.publish(account);
        this.eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    /**
     * 스터디를 종료합니다. (삭제 아님)
     * @param account
     * @param path
     */
    public void close(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.close(account);
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study, "스터디가 종료되었습니다."));
    }

    /**
     * 회원 모집을 시작합니다.
     * @param account
     * @param path
     */
    public void startRecruit(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.startRecruit(account);
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study
                , "스터디에서 팀원 모집이 시작되었습니다."));
    }

    /**
     * 회원 모집을 중단합니다.
     * @param account
     * @param path
     */
    public void stopRecruit(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.stopRecruit(account);
        this.eventPublisher.publishEvent(new StudyUpdatedEvent(study
                , "스터디에서 팀원 모집이 중단되었습니다."));
    }

    /**
     * 스터디의 경로를 수정합니다.
     * 새로운 경로의 스터디가 있는지 중복 체크 후 없으면 진행합니다.
     * @param account
     * @param path
     * @param newPath
     */
    public void changeStudyPath(Account account, String path, String newPath) {
        Study study = this.findStudyWithManagersByPath(path);

        if(!this.studyRepository.existsByPath(newPath)) {
            study.changePath(account, newPath);
        }
    }

    /**
     * 해당 경로의 스터디 명을 변경합니다.
     * @param account
     * @param path
     * @param newTitle
     */
    public void changeStudyTitle(Account account, String path, String newTitle) {
        Study study = this.findStudyWithManagersByPath(path);
        study.changeTitle(account, newTitle);
    }

    /**
     * 해당 경로의 스터디를 삭제합니다.
     * @param account
     * @param path
     */
    public void removeStudy(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.checkedManager(account);
        this.studyRepository.delete(study);
    }

    /**
     * 스터디에 해당 계정을 추가합니다. (가입)
     * @param account
     * @param path
     * @return
     */
    public Study joinStudy(Account account, String path) {
        Study study = this.findStudyWithMembersByPath(path);
        study.joinMember(account);
        return study;
    }

    /**
     * 스터디에서 해당 계정을 삭제합니다. (탈퇴)
     * @param account
     * @param path
     * @return
     */
    public Study leaveStudy(Account account, String path) {
        Study study = this.findStudyWithMembersByPath(path);
        study.leaveMember(account);
        return study;
    }

    /**
     * 해당 계정이 해당 경로의 스터디의 멤버(매니저, 일반멤버)인지 확인합니다.
     * @param path
     * @param account
     */
    public void checkedMember(String path, Account account) {
        Study study = this.studyRepository.findStudyWithManagersAndMembersByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
        if(!study.isManager(account) && !study.isMember(account)) {
            throw new IllegalArgumentException("해당 스터디의 회원이아닙니다.");
        }
    }

    /**
     * 해당 계정이 해당 스터디의 매니저인지 확인합니다.
     * @param path
     * @param account
     */
    public void checkedManager(String path, Account account) {
        Study study = this.findStudyWithManagersByPath(path);
        study.checkedManager(account);
    }

    /**
     * 키워드에 맞는 스터디를 조회 합니다.
     * (스터디명, 활동지역, 관심분야에 해당 키워드가 있는 스터디)
     * @param keyword
     * @param pageable
     * @return
     */
    public Page<Study> searchStudy(String keyword, Pageable pageable) {
        return this.studyRepository.findByKeyword(keyword, pageable);
    }

    /**
     * 최근 오픈 된 9개의 스터디를 찾아 반환합니다.
     * @return
     */
    public List<Study> findTop9ByOrderPublishedDateTime() {
        return this.studyRepository
                .findTop9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false);
    }


    /**
     * 로그인 된 계정의 관심 분야와 활동 지역을 가지고 있는 스터디를 조회합니다.
     * @param account
     * @return
     */
    public List<Study> findByZonesAndTagsForAccount(Account account) {
        Account persistAccount = this.accountRepository.findWithTagsAndZonesById(account.getId())
                .orElseThrow(IllegalAccessError::new);
        return this.studyRepository.findByTagsAndZones(persistAccount.getTags(), persistAccount.getZones());
    }

    public List<Study> findManagedStudy(Account account) {
        return this.studyRepository
                .findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
    }

    public List<Study> findJoinedStudy(Account account) {
        return this.studyRepository
                .findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
    }
}
