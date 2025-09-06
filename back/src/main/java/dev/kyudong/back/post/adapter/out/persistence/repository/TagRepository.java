package dev.kyudong.back.post.adapter.out.persistence.repository;

import dev.kyudong.back.post.domain.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {

	Set<Tag> findByNameIn(@Param("names") Set<String> name);

	@Query("""
			SELECT t.name
			FROM Tag t
			WHERE t.name LIKE :query%
			ORDER BY t.usageCount DESC, t.modifiedAt DESC, t.name
		""")
	List<String> findByNameStartingWithOrderByPopularity(@Param("query") String query, Pageable pageable);

}
