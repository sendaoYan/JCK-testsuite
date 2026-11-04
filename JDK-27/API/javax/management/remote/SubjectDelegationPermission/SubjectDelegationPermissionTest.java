/*
 * @test
 * @summary javax.management.remote.SubjectDelegationPermission conformance: white-box structure + black-box behavior.
 * @run main SubjectDelegationPermissionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.BasicPermission;

import javax.management.remote.SubjectDelegationPermission;

@SuppressWarnings("removal")
public class SubjectDelegationPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.SubjectDelegationPermission");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(BasicPermission.class.isAssignableFrom(c), "extends BasicPermission");
        check(c.getConstructor(String.class) != null, "(String) ctor");
        check(c.getConstructor(String.class, String.class) != null, "(String,String) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SubjectDelegationPermission p = new SubjectDelegationPermission("javax.management.remote.SubjectDelegation");
        check(p.getName().equals("javax.management.remote.SubjectDelegation"), "name");
        check(p.getActions().equals(""), "actions empty");
        check(p.equals(new SubjectDelegationPermission("javax.management.remote.SubjectDelegation")), "equals");
        check(p.hashCode() == new SubjectDelegationPermission("javax.management.remote.SubjectDelegation").hashCode(), "hashCode");
        check(p.implies(new SubjectDelegationPermission("javax.management.remote.SubjectDelegation")), "implies itself");
        check(!p.equals(new SubjectDelegationPermission("other.name")), "different name not equal");

        // The two-argument constructor requires null actions.
        SubjectDelegationPermission p2 = new SubjectDelegationPermission("n", null);
        check(p2.getName().equals("n"), "two-arg null-actions name");
        check(p2.getActions().equals(""), "two-arg null-actions actions");

        try {
            new SubjectDelegationPermission("n", "read");
            check(false, "expected IAE for non-null actions");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Non-null actions"), "non-null actions message: " + e.getMessage());
        }
        try {
            new SubjectDelegationPermission(null);
            check(false, "expected NPE for null name");
        } catch (NullPointerException e) {
            check(e.getMessage().equals("name can't be null"), "null name message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
