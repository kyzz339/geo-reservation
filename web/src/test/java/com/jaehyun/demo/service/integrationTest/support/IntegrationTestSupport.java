package com.jaehyun.demo.service.integrationTest.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestSupport {

    static final GenericContainer<?> REDIS_CONTAINER;
    static final PostgreSQLContainer<?> POSTGIS_CONTAINER;

    static {
        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);

        REDIS_CONTAINER.start();

        DockerImageName postgisImage = DockerImageName.parse("postgis/postgis:14-3.2")
                .asCompatibleSubstituteFor("postgres");

        POSTGIS_CONTAINER = new PostgreSQLContainer<>(postgisImage)
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        POSTGIS_CONTAINER.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry){
        registry.add("spring.data.redis.host",REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port",() -> REDIS_CONTAINER.getMappedPort(6379));

        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));

        registry.add("spring.datasource.url", POSTGIS_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGIS_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGIS_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Hibernate Spatial Dialect 설정 (PostgreSQL용으로 변경)
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.spatial.dialect.postgis.PostgisPG10Dialect");
    }

}
