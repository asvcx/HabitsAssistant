package org.habitsapp.server.migration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Getter
@Setter
@Component
public class DatabaseConfig {
    private static final String DB_PROPERTIES_PATH = "application.yml";
    private String url;
    private String username;
    private String password;
    private String schemaName;
    private String tblUsersName;
    private String tblHabitsName;
    private String tblDatesName;

    public DatabaseConfig() {
        Yaml yaml = new Yaml();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(DB_PROPERTIES_PATH)) {
            Map<String, Object> data = yaml.load(in);
            Map<String, Object> dbInfo = (Map<String, Object>) data.get("db");
            Map<String, Object> dbTable = (Map<String, Object>) data.get("table");
            this.url = ((String) dbInfo.get("url"));
            this.username = (String) dbInfo.get("username");
            this.password = (String) dbInfo.get("password");
            this.schemaName = (String) dbInfo.get("schema_name");
            this.tblUsersName = (String) dbTable.get("users_name");
            this.tblHabitsName = (String) dbTable.get("habits_name");
            this.tblDatesName = (String) dbTable.get("dates_name");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }

    }
}