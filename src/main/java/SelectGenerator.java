import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SelectGenerator {
    private Base base;
    private List<String> tables;

    public SelectGenerator(String path) throws IOException {
        parseYaml(path);
        fillTables();
    }

    private void fillTables() {
        tables = base.getSchemas().values().stream()
                .flatMap(schema -> schema.getTables().keySet().stream()
                        .map(table -> schema.getName() + "." + table)
                ).collect(Collectors.toList());
    }

    private void parseYaml(String path) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        base = mapper.readValue(new File(path), Base.class);
    }

    /**
     * tablePattern - regexp of qualified table name
     *     e.g. "sakila\.actor ", "sakila\..*", ".*\.person"
     * query - text to find in database e.g. "Alice", "42", "true"
     * caseSensitive - whether to use LIKE or ILIKE operation for varchar columns
     */
    List<String> generateSelects(String tablePattern, String query, boolean caseSensitive) {
        List<String> tablesToSelect = tables.stream()
                .filter(tableName -> Pattern.matches(tablePattern, tableName))
                .collect(Collectors.toList());

        List<String> selects = tablesToSelect.stream().map(fullTableName -> {
            String fullName[] = fullTableName.split("\\.");
            String schemaName = fullName[0];
            String tableName = fullName[1];

            List<String> s = base.getSchemas().get(schemaName).getTables().get(tableName).getColumns()
                    .stream()
                    .map(column -> getColumnWhere(column, query, caseSensitive))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            String whereStatements = String.join("\n  OR ", s);

            return String.format("SELECT * FROM %s \nWHERE %s", tableName, whereStatements);
        }).collect(Collectors.toList());

        return selects;
    }

    private String getColumnWhere(Column column, String query, boolean caseSensitive) {
        if (column.getType().equals("integer")) {
            try {
                Integer.parseInt(query);
            } catch (NumberFormatException e) {
                return null;
            }
            return String.format("%s = %s", column.getName(), query);
        } else if (column.getType().equals("date")) {
            try {
                LocalDate.parse(query);
            } catch (DateTimeParseException e) {
                return null;
            }
            return String.format("%s = '%s'", column.getName(), query);
        } else if (column.getType().equals("boolean")) {
            if (query.equals("true") || query.equals("false")) {
                return String.format("%s = %s", column.getName(), query);
            }
            return null;
        } else if (Pattern.matches("varchar\\(\\d+\\)", column.getType())) {
            if (caseSensitive) {
                return String.format("%s ILIKE '%%%s%%'", column.getName(), query);
            } else {
                return String.format("%s LIKE '%%%s%%'", column.getName(), query);
            }
        } else {
            throw new IllegalArgumentException("Unexpected type");
        }
    }
}
