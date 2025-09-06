package dev.kyudong.back.post.application.port.in;

import dev.kyudong.back.post.domain.entity.Category;

public interface CategoryUsecase {

	Category findByCategoryCode(String categoryCode);

}
