package studyolle.tag.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTitle(String tagTitle);
}
