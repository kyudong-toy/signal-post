package dev.kyudong.back.testhelper.base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트시 상속받아 사용해주세요
 */
@Tag("integration")
@Disabled
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {

	@Container
	@ServiceConnection
	private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER =
			new PostgreSQLContainer<>("postgres:15");

	@Container
	@ServiceConnection(name = "redis")
	private static final GenericContainer<?> REDIS_CONTAINER =
			new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

	@Container
	@ServiceConnection
	private static final RabbitMQContainer RABBITMQ_CONTAINER =
			new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"));

}
