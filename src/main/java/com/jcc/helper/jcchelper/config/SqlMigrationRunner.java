package com.jcc.helper.jcchelper.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

@Component
public class SqlMigrationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public SqlMigrationRunner(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void runMigrations() throws Exception {
        ensureMigrationTable();
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:db/migration/V*.sql");
        Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
        for (Resource resource : resources) {
            String fileName = resource.getFilename();
            if (fileName == null) {
                continue;
            }
            String version = extractVersion(fileName);
            if (isApplied(version)) {
                continue;
            }
            applyScript(resource);
            jdbcTemplate.update(
                    "INSERT INTO schema_migration(version, script_name) VALUES (?, ?)",
                    version, fileName
            );
        }
    }

    private void ensureMigrationTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS schema_migration (
                    version TEXT PRIMARY KEY,
                    script_name TEXT NOT NULL,
                    applied_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private boolean isApplied(String version) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM schema_migration WHERE version = ?",
                Integer.class,
                version
        );
        return count != null && count > 0;
    }

    private void applyScript(Resource script) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, script);
        }
    }

    private String extractVersion(String fileName) {
        int split = fileName.indexOf("__");
        return split > 0 ? fileName.substring(0, split) : fileName;
    }
}
