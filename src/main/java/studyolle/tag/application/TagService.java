package studyolle.tag.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyolle.account.dto.TagForm;
import studyolle.tag.domain.Tag;
import studyolle.tag.domain.TagRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag addTag(TagForm tagForm) {
        return this.tagRepository.findByTitle(tagForm.getTagTitle())
                .orElseGet(() -> this.tagRepository.save(Tag.builder().title(tagForm.getTagTitle()).build()));
    }

    public Optional<Tag> findByTitle(TagForm tagForm) {
        return this.tagRepository.findByTitle(tagForm.getTagTitle());
    }

    public List<Tag> findAllTags() {
        return this.tagRepository.findAll();
    }
}
