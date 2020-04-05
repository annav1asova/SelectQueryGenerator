import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        final SelectGenerator generator = new SelectGenerator("src/main/resources/example.yaml");
        final List<String> res = generator.generateSelects(".*\\.actor", "42", false);
        res.forEach(System.out::println);
    }
}
