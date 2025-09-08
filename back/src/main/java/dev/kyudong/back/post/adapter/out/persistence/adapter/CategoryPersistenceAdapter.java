package dev.kyudong.back.post.adapter.out.persistence.adapter;

import dev.kyudong.back.post.adapter.out.persistence.repository.CategoryRepository;
import dev.kyudong.back.post.application.port.out.web.CategoryPersistencePort;
import dev.kyudong.back.post.domain.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryPersistencePort {

	private final CategoryRepository categoryRepository;

	@Override
	@Transactional(readOnly = true)
	public Category findByCategoryCode(String categoryCode) {
		return  categoryRepository.findByCategoryCode(categoryCode)
				.orElseThrow(() -> {
					log.warn("조회되지 않은 카테고리입니다: categoryCode={}", categoryCode);
					return new RuntimeException();
				});
	}

}
