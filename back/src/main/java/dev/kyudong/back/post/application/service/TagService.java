package dev.kyudong.back.post.application.service;

import dev.kyudong.back.post.application.port.in.TagUsecase;
import dev.kyudong.back.post.application.port.out.TagPersistencePort;
import dev.kyudong.back.post.domain.entity.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService implements TagUsecase {

	private final TagPersistencePort tagPersistencePort;

	@Override
	@Transactional(readOnly = true)
	public List<String> findTagNamesByQuery(String query) {
		log.debug("태그를 조회합니다: query={}", query);
		return tagPersistencePort.findByNameStartingWithOrderByPopularity(query);
	}

	@Override
	@Transactional
	public Set<Tag> caretedNewTag(Set<String> tagNames) {
		if (tagNames.isEmpty()) {
			return new HashSet<>();
		}

		Set<Tag> existsingTags = tagPersistencePort.findByNameIn(tagNames);

		Set<String> existsingTagNames = existsingTags.stream()
				.map(Tag::getName)
				.collect(Collectors.toSet());

		for (Tag t : existsingTags) {
			t.increaseCount();
		}

		Set<Tag> newTags = tagNames.stream()
				.filter(name -> !existsingTagNames.contains(name))
				.map(Tag::of)
				.collect(Collectors.toSet());

		if (!newTags.isEmpty()) {
			Set<Tag> savedTags = tagPersistencePort.saveNewTags(newTags);
			existsingTags.addAll(savedTags);
		}

		return existsingTags;
	}

}
