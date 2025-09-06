package dev.kyudong.back.post.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@ToString(exclude = {"parent", "children", "posts"})
@Table(name = "CATEGORIES")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_ID")
	private Category parent;

	@Column(name = "CATEGORY_CODE", unique = true, nullable = false, length = 50)
	private String categoryCode;

	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CategoryTranslation> translations = new ArrayList<>();

	@OneToMany(mappedBy = "parent")
	private List<Category> children = new ArrayList<>();

	@OneToMany(mappedBy = "category")
	private List<Post> posts = new ArrayList<>();

	@CreatedDate
	@Column(name = "CREATED_AT", updatable = false)
	private Instant createdAt;

	@Builder
	private Category(String categoryCode, Category parent) {
		this.categoryCode = categoryCode;
		this.setParent(parent);
	}

	public void setParent(Category parent) {
		if (this.parent != null) {
			this.parent.getChildren().remove(this);
		}

		this.parent = parent;

		if (parent != null) {
			parent.getChildren().add(this);
		}
	}

	public void addTranslation(String languageCode, String name) {
		CategoryTranslation translation = CategoryTranslation.builder()
				.category(this)
				.languageCode(languageCode)
				.name(name)
				.build();
		this.translations.add(translation);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Category category = (Category) o;

		return Objects.equals(categoryCode, category.categoryCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(categoryCode);
	}

}
