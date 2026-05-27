package com.springboot.MyTodoList.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component("oracleDatabase")
public class OracleDatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public OracleDatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {

            if (rs.next()) {
                return Health.up()
                        .withDetail("database", "Oracle")
                        .withDetail("validationQuery", "SELECT 1 FROM DUAL")
                        .build();
            }
            return Health.down().withDetail("reason", "Validation query returned no rows").build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Oracle")
                    .withException(e)
                    .build();
        }
    }
}
