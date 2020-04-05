import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SelectGeneratorTest {
    private final String pathCorrectYaml = "src/test/yaml/example.yaml";
    private final String pathIncorrectYaml = "src/test/yaml/incorrect.yaml";

    @Test
    void testEmptyResult() throws IOException {
        List<String> expected = new ArrayList<>();

        check(pathCorrectYaml,
                ".*\\.tablename", "42", false,
                expected);
    }

    @Test
    void testBooleanQuery() throws IOException {
        List<String> expected = new ArrayList<>();
        expected.add("SELECT * FROM actor " +
                "WHERE first_name ILIKE '%true%' " +
                "OR last_name ILIKE '%true%' " +
                "OR has_kids = true");

        check(pathCorrectYaml,
                ".*\\.actor", "true", true,
                expected);
    }

    @Test
    void testDateQuery() throws IOException {
        List<String> expected = new ArrayList<>();
        expected.add("SELECT * FROM actor " +
                "WHERE first_name ILIKE '%2016-02-15%' " +
                "OR last_name ILIKE '%2016-02-15%' " +
                "OR last_update = '2016-02-15'");

        check(pathCorrectYaml,
                ".*\\.actor", "2016-02-15", true,
                expected);
    }

    @Test
    void testIncorrectDateQuery() throws IOException {
        List<String> expected = new ArrayList<>();
        expected.add("SELECT * FROM actor " +
                "WHERE first_name ILIKE '%2016-02-31%' " +
                "OR last_name ILIKE '%2016-02-31%'");

        check(pathCorrectYaml,
                ".*\\.actor", "2016-02-31", true,
                expected);
    }

    @Test
    void testIntegerQuery() throws IOException {
        List<String> expected = new ArrayList<>();
        expected.add("SELECT * FROM actor " +
                "WHERE actor_id = 42 " +
                "OR first_name LIKE '%42%' " +
                "OR last_name LIKE '%42%'");

        check(pathCorrectYaml,
                ".*\\.actor", "42", false,
                expected);
    }

    @Test
    void testIncorrectYaml() {
        Throwable thrown = assertThrows(MismatchedInputException.class, () -> {
            final SelectGenerator generator = new SelectGenerator(pathIncorrectYaml);
        });
        Assert.assertNotNull(thrown.getMessage());
    }

    private void check(String path, String tablePattern, String query, boolean caseSensitive, List<String> expected) throws IOException {
        final SelectGenerator generator = new SelectGenerator(path);
        final List<String> actual = generator.generateSelects(tablePattern, query, caseSensitive);

        Assert.assertEquals(actual.size(), expected.size(), "Number of select queries is incorrect");
        for (String selectQuery : actual) {
            Assert.assertTrue(expected.contains(normalizeSelect(selectQuery)),
                    String.format("There is extra select query:\n%s", selectQuery));
        }
    }

    private String normalizeSelect(String s) {
        return s.replace('\n', ' ').trim()
                .replaceAll(" +", " ");
    }
}
