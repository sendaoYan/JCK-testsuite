/*
 * @test
 * @summary java.nio.file.attribute.FileAttribute conformance: hierarchy and methods.
 * @run main FileAttributeTest
 */

import java.nio.file.attribute.FileAttribute;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileAttributeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileAttribute() {
        check(FileAttribute.class.isInterface(), "is interface");

        Set<String> names = new HashSet<String>();
        for (Method m : FileAttribute.class.getMethods()) names.add(m.getName());
        String[] required = { "name", "value" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileAttribute();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
