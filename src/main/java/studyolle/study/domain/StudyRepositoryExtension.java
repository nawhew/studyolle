package studyolle.study.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);
}
