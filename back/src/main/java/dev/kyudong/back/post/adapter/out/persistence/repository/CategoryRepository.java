package dev.kyudong.back.post.adapter.out.persistence.repository;

import dev.kyudong.back.post.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	Optional<Category> findByCategoryCode(String categoryCode);

}
