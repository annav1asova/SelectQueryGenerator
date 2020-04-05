import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Schema {
    private String name;
    private Map<String, Table> tables;

    public Schema() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables.stream().collect(
                Collectors.toMap(Table::getName, table -> table));
    }
}