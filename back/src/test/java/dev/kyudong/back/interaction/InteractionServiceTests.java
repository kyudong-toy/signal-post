package dev.kyudong.back.interaction;

import dev.kyudong.back.interaction.api.dto.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.dto.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.InteractionType;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.repository.InteractionRepository;
import dev.kyudong.back.interaction.service.InteractionService;
import dev.kyudong.back.post.domain.entity.Category;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.repository.PostRepository;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class InteractionServiceTests {

	@Mock
	private InteractionRepository interactionRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PostRepository postRepository;

	@InjectMocks
	private InteractionService interactionService;

	private static User makeMockUser() {
		User mockUser = User.builder()
				.username("username")
				.rawPassword("passWord")
				.encodedPassword("passWord")
				.build();
		ReflectionTestUtils.setField(mockUser, "id", 1L);
		return mockUser;
	}

	private static Post makeMockPost(User mockUser) {
		Post mockPost = Post.create("제목", "", Category.builder().build());
		ReflectionTestUtils.setField(mockPost, "id", 1L);
		ReflectionTestUtils.setField(mockPost, "user", mockUser);
		return mockPost;
	}

	@Test
	@DisplayName("상호작용 생성 - 성공")
	void doInteraction_success_create() {
		// given
		User mockUser = makeMockUser();
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

		Post mockPost = makeMockPost(mockUser);
		given(postRepository.existsById(mockPost.getId())).willReturn(true);

		Interaction newInteraction = Interaction.builder()
				.user(mockUser)
				.targetType(TargetType.POST)
				.targetId(mockPost.getId())
				.interactionType(InteractionType.LAUGH)
				.build();
		given(interactionRepository.save(any(Interaction.class))).willReturn(newInteraction);
		InteractionReqDto request = new InteractionReqDto(InteractionType.LAUGH);

		// when
		InteractionResDto response = interactionService.doInteraction(
				mockUser.getId(), TargetType.POST, mockPost.getId(), request
		);

		// then
		assertThat(response).isNotNull();
		assertThat(response.targetId()).isEqualTo(mockPost.getId());
		assertThat(response.targetType()).isEqualTo(TargetType.POST);
		assertThat(response.interactionType()).isEqualTo(InteractionType.LAUGH);
	}

	@Test
	@DisplayName("상호작용 생성 - 실패: 요청 대상을 찾을 수 없음")
	void doInteraction_fail_targetNotFound() {
		// given
		User mockUser = makeMockUser();
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

		Post mockPost = makeMockPost(mockUser);
		given(postRepository.existsById(mockPost.getId())).willReturn(false);

		InteractionReqDto request = new InteractionReqDto(InteractionType.LAUGH);

		// when & then
		assertThatThrownBy(() -> interactionService.doInteraction(mockUser.getId(), TargetType.POST, mockPost.getId(), request))
				.isInstanceOf(PostNotFoundException.class);
		then(interactionRepository).should(never()).existsByUserAndTargetId(any(User.class), anyLong());
		then(interactionRepository).should(never()).save(any(Interaction.class));
	}

	@Test
	@DisplayName("상호작용 수정 - 성공")
	void doInteraction_success_update() {
		// given
		User mockUser = makeMockUser();
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

		Post mockPost = makeMockPost(mockUser);
		given(postRepository.existsById(mockPost.getId())).willReturn(true);

		Interaction interaction = Interaction.builder()
				.user(mockUser)
				.targetType(TargetType.POST)
				.targetId(mockPost.getId())
				.interactionType(InteractionType.LAUGH)
				.build();
		given(interactionRepository.existsByUserAndTargetId(any(User.class), anyLong()))
				.willReturn(true);

		given(interactionRepository.findByUserAndTargetIdAndTargetType(any(User.class), anyLong(), any(TargetType.class)))
				.willReturn(Optional.of(interaction));

		InteractionReqDto request = new InteractionReqDto(InteractionType.ANGRY);

		// when
		InteractionResDto response = interactionService.doInteraction(
				mockUser.getId(), TargetType.POST, mockPost.getId(), request
		);

		// then
		assertThat(response).isNotNull();
		assertThat(response.targetId()).isEqualTo(mockPost.getId());
		assertThat(response.targetType()).isEqualTo(TargetType.POST);
		assertThat(response.interactionType()).isEqualTo(InteractionType.ANGRY);
	}

	@Test
	@DisplayName("상호작용 삭제  - 성공")
	void deleteInteraction_success() {
		// given
		User mockUser = makeMockUser();
		given(userRepository.getReferenceById(mockUser.getId())).willReturn(mockUser);

		Post mockPost = makeMockPost(mockUser);
		given(postRepository.existsById(mockPost.getId())).willReturn(true);

		Interaction interaction = Interaction.builder()
				.user(mockUser)
				.targetType(TargetType.POST)
				.targetId(mockPost.getId())
				.interactionType(InteractionType.LAUGH)
				.build();

		given(interactionRepository.findByUserAndTargetIdAndTargetType(any(User.class), anyLong(), any(TargetType.class)))
				.willReturn(Optional.of(interaction));

		// when
		interactionService.deleteInteraction(mockUser.getId(), TargetType.POST, mockPost.getId());

		// then
		then(interactionRepository).should().delete(any(Interaction.class));
	}

}
