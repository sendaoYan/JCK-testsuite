/*
 * @test
 * @summary java.nio.file.attribute.FileAttributeView conformance: hierarchy and methods.
 * @run main FileAttributeViewTest
 */

import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.AttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileAttributeView() {
        check(FileAttributeView.class.isInterface(), "is interface");
        check(AttributeView.class.isAssignableFrom(FileAttributeView.class), "extends AttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : FileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "name" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
