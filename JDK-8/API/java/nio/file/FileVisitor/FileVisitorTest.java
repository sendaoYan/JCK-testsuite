/*
 * @test
 * @summary java.nio.file.FileVisitor conformance: hierarchy and methods.
 * @run main FileVisitorTest
 */

import java.nio.file.FileVisitor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileVisitorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileVisitor() {
        check(FileVisitor.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : FileVisitor.class.getMethods()) names.add(m.getName());
        String[] required = { "preVisitDirectory", "visitFile", "visitFileFailed", "postVisitDirectory" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileVisitor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
