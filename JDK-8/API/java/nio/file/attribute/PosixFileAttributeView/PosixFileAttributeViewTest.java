/*
 * @test
 * @summary java.nio.file.attribute.PosixFileAttributeView conformance: hierarchy and methods.
 * @run main PosixFileAttributeViewTest
 */

import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PosixFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPosixFileAttributeView() {
        check(PosixFileAttributeView.class.isInterface(), "is interface");
        check(BasicFileAttributeView.class.isAssignableFrom(PosixFileAttributeView.class), "extends BasicFileAttributeView");
        check(FileOwnerAttributeView.class.isAssignableFrom(PosixFileAttributeView.class), "extends FileOwnerAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : PosixFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "setPermissions", "setGroup" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testPosixFileAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
