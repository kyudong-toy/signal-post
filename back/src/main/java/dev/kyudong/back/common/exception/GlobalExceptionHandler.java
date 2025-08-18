package dev.kyudong.back.common.exception;

import dev.kyudong.back.post.exception.CommentNotFoundException;
import dev.kyudong.back.post.exception.PostNotFoundException;
import dev.kyudong.back.user.exception.UserAlreadyExistsException;
import dev.kyudong.back.user.exception.UserNotFoundException;
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

}
