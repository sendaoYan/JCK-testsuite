/*
 * @test
 * @summary java.nio.file.Paths conformance: path construction.
 * @run main PathsTest
 */

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPaths() {
        Path p = Paths.get("a", "b", "c");
        check(p.getNameCount() == 3, "getNameCount 3");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("a".equals(p.getName(0).toString()), "getName(0)");
    }

    public static void main(String[] args) {
        testPaths();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
