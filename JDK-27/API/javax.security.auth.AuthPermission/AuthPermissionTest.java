/*
 * @test
 * @summary javax.security.auth.AuthPermission conformance: white-box structure + black-box behavior.
 * @run main AuthPermissionTest
 */

import java.lang.reflect.Modifier;
import javax.security.auth.AuthPermission;

public class AuthPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.AuthPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.security.BasicPermission.class, "extends BasicPermission");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AuthPermission p = new AuthPermission("doAs");
        check("doAs".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "empty actions");

        AuthPermission p2 = new AuthPermission("doAs", null);
        check("".equals(p2.getActions()), "two-arg empty actions");

        AuthPermission p3 = new AuthPermission("doAs", "ignored");
        check("".equals(p3.getActions()), "two-arg actions ignored -> empty");

        // wildcard implication
        AuthPermission star = new AuthPermission("*");
        check(star.implies(p), "star implies doAs");
        check(!p.implies(star), "doAs does not imply star");

        AuthPermission dot = new AuthPermission("doAs.*");
        check(dot.implies(new AuthPermission("doAs.sub")), "doAs.* implies doAs.sub");
        check(!dot.implies(new AuthPermission("doAs")), "doAs.* does not imply exact doAs");

        // backwards-compatibility rewrite: createLoginContext -> createLoginContext.*
        AuthPermission clc = new AuthPermission("createLoginContext");
        check("createLoginContext.*".equals(clc.getName()), "createLoginContext rewritten");
        check(clc.implies(new AuthPermission("createLoginContext.foo")), "rewritten implies child");

        // equals/hashCode symmetry for equal names
        AuthPermission a1 = new AuthPermission("modifyPrincipals");
        AuthPermission a2 = new AuthPermission("modifyPrincipals");
        check(a1.equals(a2) && a2.equals(a1), "equals symmetric");
        check(a1.hashCode() == a2.hashCode(), "equal hashCode");
        check(!a1.equals(p), "different name not equal");

        // invalid name inputs
        boolean iae = false;
        try { new AuthPermission(""); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "empty name throws IllegalArgumentException");

        boolean npe = false;
        try { new AuthPermission(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null name throws NullPointerException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
