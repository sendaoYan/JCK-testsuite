/*
 * @test
 * @summary java.lang.Runtime.Version conformance: parse/feature/compareTo.
 * @run main VersionTest
 */
import java.lang.Runtime;

public class VersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        Runtime.Version v = Runtime.Version.parse("27.0.1");
        check(v.feature() == 27, "feature 27");
        check(v.interim() == 0, "interim 0");
        check(v.update() == 1, "update 1");
        check(v.patch() == 0, "patch 0");
        check(v.compareTo(Runtime.Version.parse("27")) > 0, "compareTo greater");
        check(v.compareTo(Runtime.Version.parse("27.0.1")) == 0, "compareTo equal");
        check(v.equals(Runtime.Version.parse("27.0.1")), "equals");
        check(Runtime.version().feature() == 27, "Runtime.version feature 27");
        check(v.version().size() >= 1, "version list non-empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
