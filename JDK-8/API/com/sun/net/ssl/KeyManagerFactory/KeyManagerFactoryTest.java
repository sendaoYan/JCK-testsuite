/*
 * @test
 * @summary com.sun.net.ssl.KeyManagerFactory conformance: hierarchy and declared members via reflection.
 * @run main KeyManagerFactoryTest
 */

public class KeyManagerFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.ssl.KeyManagerFactory", false, null);
        check(c.getName().equals("com.sun.net.ssl.KeyManagerFactory"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getAlgorithm"), "declares getAlgorithm");
        check(ms.contains("getDefaultAlgorithm"), "declares getDefaultAlgorithm");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getKeyManagers"), "declares getKeyManagers");
        check(ms.contains("getProvider"), "declares getProvider");
        check(ms.contains("init"), "declares init");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
