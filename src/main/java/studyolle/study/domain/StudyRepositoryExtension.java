package studyolle.study.domain;

import java.util.List;

public interface StudyRepositoryExtension {

    List<Study> findByKeyword(String keyword);
}
