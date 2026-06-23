package com.adnane.moulcyber.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgreSQLContainerTest {

    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("moul_cyber_test")
            .withUsername("moul_cyber")
            .withPassword("moul_cyber");

    static {
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    static void configurePostgreSQL(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void truncateDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    reviews,
                    rental_items,
                    rentals,
                    game_copies,
                    games,
                    users
                RESTART IDENTITY CASCADE
                """);
    }
}
