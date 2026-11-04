/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributes conformance: hierarchy and methods.
 * @run main DosFileAttributesTest
 */

import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.BasicFileAttributes;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DosFileAttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDosFileAttributes() {
        check(DosFileAttributes.class.isInterface(), "is interface");
        check(BasicFileAttributes.class.isAssignableFrom(DosFileAttributes.class), "extends BasicFileAttributes");
        Set<String> names = new HashSet<String>();
        for (Method m : DosFileAttributes.class.getMethods()) names.add(m.getName());
        String[] required = { "isReadOnly", "isHidden", "isArchive", "isSystem" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testDosFileAttributes();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
