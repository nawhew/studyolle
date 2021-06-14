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

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;

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
}
