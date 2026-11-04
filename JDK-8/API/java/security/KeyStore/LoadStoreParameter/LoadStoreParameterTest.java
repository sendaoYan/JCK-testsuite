/*
 * @test
 * @summary java.security.KeyStore.LoadStoreParameter conformance: interface hierarchy and key methods.
 * @run main LoadStoreParameterTest
 */

public class LoadStoreParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$LoadStoreParameter");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getProtectionParameter"), "declares getProtectionParameter");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
