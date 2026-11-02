/*
 * @test
 * @summary java.security.AlgorithmConstraints conformance: interface hierarchy and key methods.
 * @run main AlgorithmConstraintsTest
 */

public class AlgorithmConstraintsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmConstraints");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("permits"), "declares permits");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
