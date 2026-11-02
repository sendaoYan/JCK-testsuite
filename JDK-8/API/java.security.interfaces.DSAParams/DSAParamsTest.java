/*
 * @test
 * @summary java.security.interfaces.DSAParams conformance: interface hierarchy and key methods.
 * @run main DSAParamsTest
 */

public class DSAParamsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.interfaces.DSAParams");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getG"), "declares getG");
        check(ms.contains("getP"), "declares getP");
        check(ms.contains("getQ"), "declares getQ");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
