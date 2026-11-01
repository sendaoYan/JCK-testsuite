/*
 * @test
 * @summary java.nio.file.attribute.FileOwnerAttributeView conformance: hierarchy and methods.
 * @run main FileOwnerAttributeViewTest
 */

import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FileOwnerAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFileOwnerAttributeView() {
        check(FileOwnerAttributeView.class.isInterface(), "is interface");
        check(FileAttributeView.class.isAssignableFrom(FileOwnerAttributeView.class), "extends FileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : FileOwnerAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "getOwner", "setOwner" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testFileOwnerAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
