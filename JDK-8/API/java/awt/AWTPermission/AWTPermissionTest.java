/*
 * @test
 * @summary java.awt.AWTPermission conformance: class reflection.
 * @run main AWTPermissionTest
 */

public class AWTPermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.AWTPermission");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass java.security.BasicPermission");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // AWTPermission: BasicPermission semantics
        java.awt.AWTPermission p1 = new java.awt.AWTPermission("listenToAllAWTEvents");
        check(p1.getName().equals("listenToAllAWTEvents"), "getName");
        check(p1.getActions().equals(""), "getActions empty string");
        check(p1.equals(new java.awt.AWTPermission("listenToAllAWTEvents")), "equals same name");
        check(p1.hashCode() == new java.awt.AWTPermission("listenToAllAWTEvents").hashCode(), "hashCode");
        check(p1.implies(p1), "implies self");
        check(new java.awt.AWTPermission("*").implies(p1), "wildcard implies specific");
        check(!p1.implies(new java.awt.AWTPermission("*")), "specific does not imply wildcard");
        check(!p1.implies(new java.awt.AWTPermission("other")), "unrelated not implied");
        check(!p1.equals(null), "equals(null)");
        boolean npe = false;
        try { new java.awt.AWTPermission(null); } catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "null name -> NPE");
        check(new java.awt.AWTPermission("a.b.c").getName().equals("a.b.c"), "dotted name accepted");

    }
}
