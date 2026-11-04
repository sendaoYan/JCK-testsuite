/*
 * @test
 * @summary java.sql.SQLPermission conformance: white-box structure + black-box behavior.
 * @run main SQLPermissionTest
 */
public class SQLPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Throwing { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> t, String label, Throwing r) {
        try { r.run(); } catch (Throwable x) { check(t.isInstance(x), label + " throws " + t.getSimpleName()); return; }
        throw new AssertionError(label + ": expected " + t.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.sql.SQLPermission");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass");
        check(c.getInterfaces().length == 0, "no direct superinterface");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.sql.SQLPermission p = new java.sql.SQLPermission("setLog");
        check("setLog".equals(p.getName()), "getName");
        check("".equals(p.getActions()), "getActions empty");
        java.sql.SQLPermission p2 = new java.sql.SQLPermission("setLog", null);
        check("setLog".equals(p2.getName()), "2-arg getName");
        check(p.implies(p2), "implies self");
        check(p2.implies(p), "implies symmetric");
        java.sql.SQLPermission p3 = new java.sql.SQLPermission("callAbort");
        check("callAbort".equals(p3.getName()), "distinct name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
