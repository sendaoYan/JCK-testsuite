/*
 * @test
 * @summary javax.security.sasl.SaslClientFactory conformance: interface hierarchy and key methods.
 * @run main SaslClientFactoryTest
 */

public class SaslClientFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslClientFactory");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("createSaslClient"), "declares createSaslClient");
        check(ms.contains("getMechanismNames"), "declares getMechanismNames");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
