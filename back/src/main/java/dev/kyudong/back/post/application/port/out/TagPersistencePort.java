package dev.kyudong.back.post.application.port.out;

import dev.kyudong.back.post.domain.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagPersistencePort {

	Set<Tag> findByNameIn(Set<String> tags);

	Set<Tag> saveNewTags(Set<Tag> tags);

	List<String> findByNameStartingWithOrderByPopularity(String query);

}
