/*
 * @test
 * @summary java.nio.file.attribute.GroupPrincipal conformance: hierarchy and methods.
 * @run main GroupPrincipalTest
 */

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class GroupPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testGroupPrincipal() {
        check(GroupPrincipal.class.isInterface(), "is interface");
        check(UserPrincipal.class.isAssignableFrom(GroupPrincipal.class), "extends UserPrincipal");
        Set<String> names = new HashSet<String>();
        for (Method m : GroupPrincipal.class.getMethods()) names.add(m.getName());
        String[] required = { "getName" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testGroupPrincipal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
