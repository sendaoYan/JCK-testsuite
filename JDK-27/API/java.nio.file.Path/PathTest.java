/*
 * @test
 * @summary java.nio.file.Path conformance: components and resolution.
 * @run main PathTest
 */

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPath() {
        Path p = Paths.get("/a/b/c");
        check(p.isAbsolute(), "isAbsolute");
        check("/".equals(p.getRoot().toString()), "getRoot");
        check("c".equals(p.getFileName().toString()), "getFileName");
        check("/a/b".equals(p.getParent().toString()), "getParent");
        check(p.getNameCount() == 3, "getNameCount");
        check("a".equals(p.getName(0).toString()), "getName(0)");
        check("/a/b/c".equals(p.normalize().toString()), "normalize");
        check("/a/b/c/d".equals(p.resolve("d").toString()), "resolve");
        check(p.toAbsolutePath().isAbsolute(), "toAbsolutePath");
        check(p.getFileSystem() != null, "getFileSystem");
        check(p.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testPath();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
