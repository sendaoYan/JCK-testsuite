/*
 * @test
 * @summary java.security.Permission conformance: abstract class hierarchy and key methods.
 * @run main PermissionTest
 */

public class PermissionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Permission");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.Guard"), "implements java.security.Guard");
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkGuard"), "declares checkGuard");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getActions"), "declares getActions");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("implies"), "declares implies");
        check(ms.contains("newPermissionCollection"), "declares newPermissionCollection");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
