/*
 * @test
 * @summary java.nio.file.LinkPermission conformance: white-box structure + black-box name/actions/equality.
 * @run main LinkPermissionTest
 */

import java.lang.reflect.Modifier;
import java.nio.file.LinkPermission;
import java.security.BasicPermission;

public class LinkPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.LinkPermission");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == BasicPermission.class, "superclass is BasicPermission");
        check(c.getConstructor(String.class) != null, "String constructor present");
        check(c.getConstructor(String.class, String.class) != null, "String,String constructor present");
    }

    static void testBlackBox() {
        LinkPermission p = new LinkPermission("hard");
        check("hard".equals(p.getName()), "getName hard");
        check("".equals(p.getActions()), "getActions");
        check(p instanceof BasicPermission, "is BasicPermission");
        check(p.equals(new LinkPermission("hard")), "equals hard");
        check(!p.equals(new LinkPermission("symbolic")), "hard != symbolic");
        check(p.hashCode() == new LinkPermission("hard").hashCode(), "equal hashCode");
        check(p.implies(new LinkPermission("hard")), "implies hard");

        LinkPermission sym = new LinkPermission("symbolic");
        check("symbolic".equals(sym.getName()), "getName symbolic");
        check(sym.implies(new LinkPermission("symbolic")), "implies symbolic");
        check(!sym.implies(p), "symbolic does not imply hard");

        // Two-arg constructor: empty or null actions are accepted.
        LinkPermission hardEmpty = new LinkPermission("hard", "");
        check("hard".equals(hardEmpty.getName()), "two-arg name hard");
        check("".equals(hardEmpty.getActions()), "two-arg empty actions");
        check(hardEmpty.equals(p), "two-arg empty actions equals one-arg");

        LinkPermission symbolicNull = new LinkPermission("symbolic", null);
        check("symbolic".equals(symbolicNull.getName()), "two-arg null actions name");
        check("".equals(symbolicNull.getActions()), "two-arg null actions empty");

        expect(IllegalArgumentException.class, () -> new LinkPermission("foo"), "invalid name throws");
        expect(IllegalArgumentException.class, () -> new LinkPermission("hard", "read"), "non-empty actions throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
