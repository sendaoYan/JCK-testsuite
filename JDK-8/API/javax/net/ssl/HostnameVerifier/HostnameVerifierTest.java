/*
 * @test
 * @summary javax.net.ssl.HostnameVerifier conformance: interface hierarchy and key methods.
 * @run main HostnameVerifierTest
 */

public class HostnameVerifierTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HostnameVerifier");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("verify"), "declares verify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
