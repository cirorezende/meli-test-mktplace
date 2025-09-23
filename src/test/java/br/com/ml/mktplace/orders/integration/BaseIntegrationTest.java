package br.com.ml.mktplace.orders.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for integration tests spinning up required infrastructure.
 * Containers are static so they are shared across test classes in the same JVM run.
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
public abstract class BaseIntegrationTest {

    // PostgreSQL
    // Use PostGIS-enabled image to support CREATE EXTENSION postgis in Flyway migrations
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres")
    ).withDatabaseName("mktplace_orders")
     .withUsername("postgres")
     .withPassword("postgres");

    // Redis
    protected static final GenericContainer<?> REDIS = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    // Kafka
    protected static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.1")
    );

    @BeforeAll
    static void startContainers() {
        if (!POSTGRES.isRunning()) POSTGRES.start();
        if (!REDIS.isRunning()) REDIS.start();
        if (!KAFKA.isRunning()) KAFKA.start();
    }

    @AfterAll
    static void stopContainers() {
        // Containers left running intentionally for potential reuse inside same JVM session
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Database
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl());
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        // Redis (use spring.redis.* so CacheConfig @Value picks up)
        registry.add("spring.redis.host", () -> REDIS.getHost());
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(6379));
        // Ensure tests use no password (image has no auth) and default DB 0
        registry.add("spring.redis.password", () -> "");
        registry.add("spring.redis.database", () -> 0);

        // Kafka
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

        // External API base URL (WireMock placeholder - will be overridden per test class when server started)
        registry.add("app.distribution-center.base-url", () -> "http://localhost:9999");
    }
}
