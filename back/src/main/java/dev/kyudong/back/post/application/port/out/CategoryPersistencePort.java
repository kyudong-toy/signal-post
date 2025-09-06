package dev.kyudong.back.post.application.port.out;

import dev.kyudong.back.post.domain.entity.Category;

public interface CategoryPersistencePort {

	Category findByCategoryCode(String categoryCode);

}
