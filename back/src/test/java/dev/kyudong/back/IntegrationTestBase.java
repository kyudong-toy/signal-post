package dev.kyudong.back;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Disabled
@Testcontainers
@Transactional
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

}
