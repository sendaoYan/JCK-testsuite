/*
 * @test
 * @summary java.security.SecurityPermission conformance: full contract coverage.
 *          name/name+actions constructors, getName/getActions, null name -> NPE,
 *          empty name -> IAE, implies exact and wildcard, equals/hashCode, and
 *          subclass relationship to BasicPermission.
 * @run main SecurityPermissionTest
 */

import java.security.BasicPermission;
import java.security.SecurityPermission;

public class SecurityPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    public static void main(String[] args) throws Exception {
        // ---- subclass hierarchy ----
        check(BasicPermission.class.isAssignableFrom(SecurityPermission.class),
                "SecurityPermission extends BasicPermission");

        // ---- constructors + getters ----
        SecurityPermission sp = new SecurityPermission("getPolicy");
        check("getPolicy".equals(sp.getName()), "getName");
        check("".equals(sp.getActions()), "getActions empty");
        SecurityPermission spa = new SecurityPermission("setPolicy", "ignored");
        check("setPolicy".equals(spa.getName()), "(name,actions) getName");

        // ---- null/empty name ----
        checkThrows(NullPointerException.class, () -> new SecurityPermission(null), "SecurityPermission(null)");
        checkThrows(IllegalArgumentException.class, () -> new SecurityPermission(""), "SecurityPermission(empty)");

        // ---- implies ----
        check(sp.implies(new SecurityPermission("getPolicy")), "implies exact match");
        check(!sp.implies(new SecurityPermission("setPolicy")), "implies different name false");
        SecurityPermission wildcard = new SecurityPermission("authProvider.*");
        check(wildcard.implies(new SecurityPermission("authProvider.x509")), "wildcard implies sub");

        // ---- equals/hashCode ----
        check(sp.equals(new SecurityPermission("getPolicy")), "equals same name");
        check(sp.hashCode() == new SecurityPermission("getPolicy").hashCode(), "hashCode equal");
        check(!sp.equals(new SecurityPermission("setPolicy")), "equals different name false");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
