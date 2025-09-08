package dev.kyudong.back.post.application.port.in.web;


import dev.kyudong.back.post.domain.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagUsecase {

	Set<Tag> caretedNewTag(Set<String> tags);

	List<String> findTagNamesByQuery(String query);

}
