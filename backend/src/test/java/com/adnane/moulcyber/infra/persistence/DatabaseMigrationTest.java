package com.adnane.moulcyber.infra.persistence;

import com.adnane.moulcyber.support.PostgreSQLContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseMigrationTest extends PostgreSQLContainerTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayAppliesInitialSchema() {
        Integer successfulMigrations = jdbcTemplate.queryForObject("""
                select count(*)
                from flyway_schema_history
                where version = '1'
                  and success = true
                """, Integer.class);

        assertThat(successfulMigrations).isEqualTo(1);
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("games")).isTrue();
        assertThat(tableExists("game_copies")).isTrue();
        assertThat(tableExists("rentals")).isTrue();
        assertThat(tableExists("rental_items")).isTrue();
        assertThat(tableExists("reviews")).isTrue();
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from information_schema.tables
                    where table_schema = 'public'
                      and table_name = ?
                )
                """, Boolean.class, tableName);
        return Boolean.TRUE.equals(exists);
    }
}
