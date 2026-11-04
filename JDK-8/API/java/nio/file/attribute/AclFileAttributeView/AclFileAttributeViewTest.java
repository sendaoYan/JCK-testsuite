/*
 * @test
 * @summary java.nio.file.attribute.AclFileAttributeView conformance: hierarchy and methods.
 * @run main AclFileAttributeViewTest
 */

import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AclFileAttributeViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAclFileAttributeView() {
        check(AclFileAttributeView.class.isInterface(), "is interface");
        check(FileOwnerAttributeView.class.isAssignableFrom(AclFileAttributeView.class), "extends FileOwnerAttributeView");
        Set<String> names = new HashSet<String>();
        for (Method m : AclFileAttributeView.class.getMethods()) names.add(m.getName());
        String[] required = { "getAcl", "setAcl" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testAclFileAttributeView();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
