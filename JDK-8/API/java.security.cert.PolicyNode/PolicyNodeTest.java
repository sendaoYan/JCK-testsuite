/*
 * @test
 * @summary java.security.cert.PolicyNode conformance: interface hierarchy and key methods.
 * @run main PolicyNodeTest
 */

public class PolicyNodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.PolicyNode");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getDepth"), "declares getDepth");
        check(ms.contains("getExpectedPolicies"), "declares getExpectedPolicies");
        check(ms.contains("getParent"), "declares getParent");
        check(ms.contains("getValidPolicy"), "declares getValidPolicy");
        check(ms.contains("isCritical"), "declares isCritical");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
