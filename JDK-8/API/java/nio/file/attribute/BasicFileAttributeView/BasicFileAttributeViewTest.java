/*
 * @test
 * @summary java.nio.file.attribute.BasicFileAttributeView conformance: hierarchy and methods.
 * @run main BasicFileAttributeViewTest
 */

import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BasicFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasicFileAttributeView() {
        check(BasicFileAttributeView.class.isInterface(), "is interface");
        check(FileAttributeView.class.isAssignableFrom(BasicFileAttributeView.class), "extends FileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : BasicFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "readAttributes", "setTimes" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testBasicFileAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
