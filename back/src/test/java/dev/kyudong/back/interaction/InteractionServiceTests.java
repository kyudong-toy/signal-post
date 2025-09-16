package dev.kyudong.back.interaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.kyudong.back.interaction.api.dto.req.InteractionReqDto;
import dev.kyudong.back.interaction.api.dto.res.InteractionResDto;
import dev.kyudong.back.interaction.domain.Interaction;
import dev.kyudong.back.interaction.domain.InteractionType;
import dev.kyudong.back.interaction.domain.TargetType;
import dev.kyudong.back.interaction.repository.InteractionRepository;
import dev.kyudong.back.interaction.service.InteractionService;
import dev.kyudong.back.interaction.strategy.InteractionStrategy;
import dev.kyudong.back.interaction.strategy.PostInteractionStrategy;
import dev.kyudong.back.post.domain.entity.Post;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.testhelper.base.UnitTestBase;
import dev.kyudong.back.user.domain.User;
import dev.kyudong.back.user.service.UserReaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

public class InteractionServiceTests extends UnitTestBase {

	@Mock
	private InteractionRepository interactionRepository;

	@Mock
	private PostInteractionStrategy postInteractionStrategy;

	@Mock
	private UserReaderService userReaderService;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private InteractionService interactionService;

	@BeforeEach
	void setUp() {
		List<InteractionStrategy> strategies = List.of(postInteractionStrategy);
		interactionService = new InteractionService(
				interactionRepository,
				userReaderService,
				strategies,
				eventPublisher
		);
	}

	@Nested
	@DisplayName("상호작용 생성 및 수정")
	class DoInteraction {

		@Test
		@DisplayName("성공 : 첫 상호작용")
		void success_create() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(mockUser.getId())).willReturn(mockUser);

			Post mockPost = createMockPost(mockUser);
			given(postInteractionStrategy.supports(TargetType.POST)).willReturn(true);
			doNothing().when(postInteractionStrategy).existsTarget(anyLong());

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
		@DisplayName("성공 : 상호작용 수정")
		void success_update() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(mockUser.getId())).willReturn(mockUser);

			Post mockPost = createMockPost(mockUser);
			given(postInteractionStrategy.supports(TargetType.POST)).willReturn(true);
			doNothing().when(postInteractionStrategy).existsTarget(anyLong());

			Interaction interaction = Interaction.builder()
					.user(mockUser)
					.targetType(TargetType.POST)
					.targetId(mockPost.getId())
					.interactionType(InteractionType.LAUGH)
					.build();
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
		@DisplayName("실패 : 요청 대상을 찾을 수 없음")
		void fail_targetNotFound() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();

			Post mockPost = createMockPost(mockUser);
			given(postInteractionStrategy.supports(TargetType.POST)).willReturn(true);
			doThrow(new PostNotFoundException(mockPost.getId())).when(postInteractionStrategy).existsTarget(anyLong());

			InteractionReqDto request = new InteractionReqDto(InteractionType.LAUGH);

			// when & then
			assertThatThrownBy(() -> interactionService.doInteraction(mockUser.getId(), TargetType.POST, mockPost.getId(), request))
					.isInstanceOf(PostNotFoundException.class);
			then(interactionRepository).should(never()).save(any(Interaction.class));
		}

	}

	@Nested
	@DisplayName("상호작용 삭제")
	class DeleteInteraction {

		@Test
		@DisplayName("상호작용 삭제  - 성공")
		void deleteInteraction_success() throws JsonProcessingException {
			// given
			User mockUser = createMockUser();
			given(userReaderService.getUserReference(mockUser.getId())).willReturn(mockUser);

			Post mockPost = createMockPost(mockUser);
			given(postInteractionStrategy.supports(TargetType.POST)).willReturn(true);
			doNothing().when(postInteractionStrategy).existsTarget(anyLong());

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

}
