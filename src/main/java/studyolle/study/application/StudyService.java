package studyolle.study.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.domain.Account;
import studyolle.account.domain.AccountRepository;
import studyolle.study.domain.Study;
import studyolle.study.domain.StudyRepository;
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
}
