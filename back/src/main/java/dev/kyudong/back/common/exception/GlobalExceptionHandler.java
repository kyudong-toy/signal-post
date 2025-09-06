package dev.kyudong.back.common.exception;

import dev.kyudong.back.chat.exception.ChatMessageNotFoundException;
import dev.kyudong.back.chat.exception.ChatMemberExistsException;
import dev.kyudong.back.chat.exception.ChatMemberNotFoundException;
import dev.kyudong.back.chat.exception.ChatRoomNotFoundException;
import dev.kyudong.back.file.exception.FileMetadataNotFoundException;
import dev.kyudong.back.file.exception.InvalidFileException;
import dev.kyudong.back.follow.exception.AlreadyFollowException;
import dev.kyudong.back.follow.exception.FollowingException;
import dev.kyudong.back.interaction.exception.InteractionNotFoundException;
import dev.kyudong.back.interaction.exception.InteractionTargetNotFoundException;
import dev.kyudong.back.notification.exception.NotificationNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.exception.CommentNotFoundException;
import dev.kyudong.back.post.adapter.out.persistence.exception.PostNotFoundException;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
import dev.kyudong.back.user.exception.UsersNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	protected ResponseEntity<ProblemDetail> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
		problemDetail.setTitle("Duplicate User");
		problemDetail.setStatus(HttpStatus.CONFLICT);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
	}

	@ExceptionHandler(UserNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("User Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(InvalidInputException.class)
	protected ResponseEntity<ProblemDetail> handleInvalidInputExceptionException(InvalidInputException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		problemDetail.setTitle("Invalid Input Value");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(PostNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handlePostNotFoundException(PostNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Post Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(InvalidAccessException.class)
	protected ResponseEntity<ProblemDetail> handleInvalidAccessException(InvalidAccessException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
		problemDetail.setTitle("Access Denied");
		problemDetail.setStatus(HttpStatus.UNAUTHORIZED);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
	}

	@ExceptionHandler(CommentNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleCommentNotFoundException(CommentNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Comment Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(InvalidFileException.class)
	protected ResponseEntity<ProblemDetail> handleCommentNotFoundException(InvalidFileException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		problemDetail.setTitle("Invalid Request File");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(FileMetadataNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleCommentNotFoundException(FileMetadataNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("File Metadata Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(AlreadyFollowException.class)
	protected ResponseEntity<ProblemDetail> handleAlreadyFollowException(AlreadyFollowException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		problemDetail.setTitle("Already Follow Relation");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(FollowingException.class)
	protected ResponseEntity<ProblemDetail> handleFollowingException(FollowingException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		problemDetail.setTitle("Following Exception");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(InteractionTargetNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleTargetNotFoundException(InteractionTargetNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
		problemDetail.setTitle("Interaction Target Not Found");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(InteractionNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleTargetNotFoundException(InteractionNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Interaction Not Found");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(NotificationNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleNotificationNotFoundException(NotificationNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Notification Not Found");
		problemDetail.setStatus(HttpStatus.BAD_REQUEST);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
	}

	@ExceptionHandler(UsersNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleUsersNotFoundException(UsersNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Users Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(ChatMessageNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleChatMessageNotFoundException(ChatMessageNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Chat Message Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(ChatMemberExistsException.class)
	protected ResponseEntity<ProblemDetail> handleChatMemberExistsException(ChatMemberExistsException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Chat Member Not Exists");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(ChatRoomNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleChatRoomNotFoundException(ChatRoomNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Chat Room Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

	@ExceptionHandler(ChatMemberNotFoundException.class)
	protected ResponseEntity<ProblemDetail> handleChatMemberNotFoundException(ChatMemberNotFoundException e) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
		problemDetail.setTitle("Chat Member Not Found");
		problemDetail.setStatus(HttpStatus.NOT_FOUND);
		problemDetail.setDetail(e.getMessage());
		problemDetail.setProperty("timestamp", Instant.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
	}

}
