/*
 * @test
 * @summary javax.security.sasl.SaslClient conformance: interface hierarchy and key methods.
 * @run main SaslClientTest
 */

public class SaslClientTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.sasl.SaslClient");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("dispose"), "declares dispose");
        check(ms.contains("evaluateChallenge"), "declares evaluateChallenge");
        check(ms.contains("getMechanismName"), "declares getMechanismName");
        check(ms.contains("getNegotiatedProperty"), "declares getNegotiatedProperty");
        check(ms.contains("hasInitialResponse"), "declares hasInitialResponse");
        check(ms.contains("isComplete"), "declares isComplete");
        check(ms.contains("unwrap"), "declares unwrap");
        check(ms.contains("wrap"), "declares wrap");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
