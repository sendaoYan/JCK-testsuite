/*
 * @test
 * @summary java.nio.file.attribute.PosixFileAttributes conformance: hierarchy and methods.
 * @run main PosixFileAttributesTest
 */

import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.BasicFileAttributes;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PosixFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPosixFileAttributes() {
        check(PosixFileAttributes.class.isInterface(), "is interface");
        check(BasicFileAttributes.class.isAssignableFrom(PosixFileAttributes.class), "extends BasicFileAttributes");
        Set<String> names = new HashSet<String>();
        for (Method m : PosixFileAttributes.class.getMethods()) names.add(m.getName());
        String[] required = { "permissions", "owner", "group" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testPosixFileAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
