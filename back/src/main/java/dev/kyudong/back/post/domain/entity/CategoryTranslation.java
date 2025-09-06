package dev.kyudong.back.post.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
		name = "CATEGORY_TRANSLATIONS",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_category_lang_code",
						columnNames = {"CATEGORY_ID", "LANGUAGE_CODE"}
				)
		}
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CATEGORY_ID", nullable = false)
	private Category category;

	@Column(name = "LANGUAGE_CODE", nullable = false, length = 10)
	private String languageCode;

	@Column(name = "NAME", nullable = false, length = 50)
	private String name;

	@Builder
	private CategoryTranslation(Category category, String languageCode, String name) {
		this.category = category;
		this.languageCode = languageCode;
		this.name = name;
	}

}
