/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipal conformance: hierarchy and methods.
 * @run main UserPrincipalTest
 */

import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUserPrincipal() {
        check(UserPrincipal.class.isInterface(), "is interface");
        check(Principal.class.isAssignableFrom(UserPrincipal.class), "extends Principal");
        Set<String> names = new HashSet<String>();
        for (Method m : UserPrincipal.class.getMethods()) names.add(m.getName());
        String[] required = { "getName" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testUserPrincipal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
