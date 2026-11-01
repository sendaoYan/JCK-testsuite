/*
 * @test
 * @summary java.nio.file.spi.FileTypeDetector conformance: hierarchy and methods.
 * @run main FileTypeDetectorTest
 */

import java.nio.file.spi.FileTypeDetector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileTypeDetectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileTypeDetector() {
        check(Modifier.isAbstract(FileTypeDetector.class.getModifiers()), "is abstract");

        Set<String> names = new HashSet<String>();
        for (Method m : FileTypeDetector.class.getMethods()) names.add(m.getName());
        String[] required = { "probeContentType" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileTypeDetector();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
