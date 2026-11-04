/*
 * @test
 * @summary javax.net.ssl.TrustManagerFactorySpi conformance: abstract class hierarchy and key methods.
 * @run main TrustManagerFactorySpiTest
 */

public class TrustManagerFactorySpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.TrustManagerFactorySpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineGetTrustManagers"), "declares engineGetTrustManagers");
        check(ms.contains("engineInit"), "declares engineInit");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
