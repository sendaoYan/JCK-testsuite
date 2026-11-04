/*
 * @test
 * @summary org.ietf.jgss.GSSName conformance: interface hierarchy and key methods.
 * @run main GSSNameTest
 */

public class GSSNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSName");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("canonicalize"), "declares canonicalize");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("export"), "declares export");
        check(ms.contains("getStringNameType"), "declares getStringNameType");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("isAnonymous"), "declares isAnonymous");
        check(ms.contains("isMN"), "declares isMN");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
