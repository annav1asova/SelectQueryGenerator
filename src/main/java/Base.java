import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Base {
    private Map<String, Schema> schemas;

    public Base() {
    }

    public Map<String, Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas.stream().collect(
                Collectors.toMap(Schema::getName, schema -> schema));
    }
}
