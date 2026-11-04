/*
 * @test
 * @summary java.nio.file.attribute.UserPrincipalLookupService conformance: hierarchy and methods.
 * @run main UserPrincipalLookupServiceTest
 */

import java.nio.file.attribute.UserPrincipalLookupService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipalLookupServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUserPrincipalLookupService() {
        check(Modifier.isAbstract(UserPrincipalLookupService.class.getModifiers()), "is abstract");

        Set<String> names = new HashSet<String>();
        for (Method m : UserPrincipalLookupService.class.getMethods()) names.add(m.getName());
        String[] required = { "lookupPrincipalByName", "lookupPrincipalByGroupName" };
        for (String n : required) check(names.contains(n), "has method " + n);
    }

    public static void main(String[] args) {
        testUserPrincipalLookupService();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
