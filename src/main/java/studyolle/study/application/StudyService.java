package studyolle.study.application;

import lombok.RequiredArgsConstructor;
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

import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;

    /**
     * 입력받은 폼으로 새로운 스터디를 개설합니다.
     * 생성한 계정을 관리자, 멤버로 추가합니다.
     * @param account
     * @param studyForm
     */
    public Study createStudy(Account account, StudyForm studyForm) {
        return this.studyRepository.save(studyForm.toEntity().addCreateMember(account));
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

    public void publish(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.publish(account);
    }

    public void close(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.close(account);
    }

    public void startRecruit(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.startRecruit(account);
    }

    public void stopRecruit(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.stopRecruit(account);
    }

    public void changeStudyPath(Account account, String path, String newPath) {
        Study study = this.findStudyWithManagersByPath(path);

        if(!this.studyRepository.existsByPath(newPath)) {
            study.changePath(account, newPath);
        }
    }

    public void changeStudyTitle(Account account, String path, String newTitle) {
        Study study = this.findStudyWithManagersByPath(path);
        study.changeTitle(account, newTitle);
    }

    public void removeStudy(Account account, String path) {
        Study study = this.findStudyWithManagersByPath(path);
        study.checkedManager(account);
        this.studyRepository.delete(study);
    }

    public Study joinStudy(Account account, String path) {
        Study study = this.findStudyWithMembersByPath(path);
        study.joinMember(account);
        return study;
    }

    public Study leaveStudy(Account account, String path) {
        Study study = this.findStudyWithMembersByPath(path);
        study.leaveMember(account);
        return study;
    }

    public Study findByPathCheckedMember(String path, Account account) {
        Study study = this.studyRepository.findStudyWithManagersAndMembersByPath(path)
                .orElseThrow(() -> new IllegalArgumentException(path + ": 해당 경로의 스터디가 없습니다."));
        if(!study.isManager(account) && study.isMember(account)) {
            throw new IllegalArgumentException("해당 스터디의 회원이아닙니다.");
        }
        return study;
    }
}
