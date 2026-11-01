/*
 * @test
 * @summary java.nio.file.attribute.UserDefinedFileAttributeView conformance: hierarchy and methods.
 * @run main UserDefinedFileAttributeViewTest
 */

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UserDefinedFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUserDefinedFileAttributeView() {
        check(UserDefinedFileAttributeView.class.isInterface(), "is interface");
        check(FileAttributeView.class.isAssignableFrom(UserDefinedFileAttributeView.class), "extends FileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : UserDefinedFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "list", "size", "read", "write", "delete" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testUserDefinedFileAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
