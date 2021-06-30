package studyolle.study.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyolle.tag.domain.Tag;
import studyolle.zone.domain.Zone;

import java.util.List;
import java.util.Set;

public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByTagsAndZones(Set<Tag> tags, Set<Zone> zones);
}
