/*
 * @test
 * @summary java.nio.file.attribute.DosFileAttributeView conformance: hierarchy and methods.
 * @run main DosFileAttributeViewTest
 */

import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DosFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDosFileAttributeView() {
        check(DosFileAttributeView.class.isInterface(), "is interface");
        check(BasicFileAttributeView.class.isAssignableFrom(DosFileAttributeView.class), "extends BasicFileAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : DosFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "setReadOnly", "setHidden", "setArchive", "setSystem" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testDosFileAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
