/*
 * @test
 * @summary java.security.IdentityScope conformance: abstract class hierarchy and key methods.
 * @run main IdentityScopeTest
 */

public class IdentityScopeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.IdentityScope");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.Identity"), "extends java.security.Identity");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addIdentity"), "declares addIdentity");
        check(ms.contains("getIdentity"), "declares getIdentity");
        check(ms.contains("getSystemScope"), "declares getSystemScope");
        check(ms.contains("identities"), "declares identities");
        check(ms.contains("removeIdentity"), "declares removeIdentity");
        check(ms.contains("setSystemScope"), "declares setSystemScope");
        check(ms.contains("size"), "declares size");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
