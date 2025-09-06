package dev.kyudong.back.post.application.service;

import dev.kyudong.back.post.application.port.in.CategoryUsecase;
import dev.kyudong.back.post.application.port.out.CategoryPersistencePort;
import dev.kyudong.back.post.domain.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUsecase {

	private final CategoryPersistencePort categoryPersistencePort;

	@Override
	@Transactional(readOnly = true)
	public Category findByCategoryCode(String categoryCode) {
		return categoryPersistencePort.findByCategoryCode(categoryCode);
	}

}
