/*
 * @test
 * @summary org.ietf.jgss.GSSCredential conformance: interface hierarchy and key methods.
 * @run main GSSCredentialTest
 */

public class GSSCredentialTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSCredential");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("dispose"), "declares dispose");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getMechs"), "declares getMechs");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getRemainingAcceptLifetime"), "declares getRemainingAcceptLifetime");
        check(ms.contains("getRemainingInitLifetime"), "declares getRemainingInitLifetime");
        check(ms.contains("getRemainingLifetime"), "declares getRemainingLifetime");
        check(ms.contains("getUsage"), "declares getUsage");
        check(ms.contains("hashCode"), "declares hashCode");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
