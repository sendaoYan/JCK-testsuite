/*
 * @test
 * @summary javax.security.auth.Destroyable conformance: interface hierarchy and key methods.
 * @run main DestroyableTest
 */

public class DestroyableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.auth.Destroyable");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("destroy"), "declares destroy");
        check(ms.contains("isDestroyed"), "declares isDestroyed");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
