/*
 * @test
 * @summary java.text.ListFormat.Type conformance: enum STANDARD/OR/UNIT.
 * @run main TypeTest
 */
import java.text.ListFormat;

public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ListFormat.Type.class.isEnum(), "is enum");
        check(ListFormat.Type.STANDARD != null, "STANDARD");
        check(ListFormat.Type.OR != null, "OR");
        check(ListFormat.Type.UNIT != null, "UNIT");
        check(ListFormat.Type.values().length == 3, "three constants");
        check(ListFormat.Type.valueOf("OR") == ListFormat.Type.OR, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
