package dev.kyudong.back.post.adapter.out.persistence.adapter;

import dev.kyudong.back.post.adapter.out.persistence.repository.TagRepository;
import dev.kyudong.back.post.application.port.out.TagPersistencePort;
import dev.kyudong.back.post.domain.entity.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TagPersistenceAdapter implements TagPersistencePort {

	private final TagRepository tagRepository;

	@Override
	@Transactional(readOnly = true)
	public Set<Tag> findByNameIn(Set<String> tags) {
		return tagRepository.findByNameIn(tags);
	}

	@Override
	@Transactional
	public Set<Tag> saveNewTags(Set<Tag> tags) {
		return new HashSet<>(tagRepository.saveAll(tags));
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> findByNameStartingWithOrderByPopularity(String query) {
		Pageable pageable = PageRequest.of(0, 10);
		return tagRepository.findByNameStartingWithOrderByPopularity(query, pageable);
	}

}
