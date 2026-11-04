/*
 * @test
 * @summary javax.net.ssl.SNIServerName conformance: abstract class hierarchy and key methods.
 * @run main SNIServerNameTest
 */

public class SNIServerNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIServerName");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getEncoded"), "declares getEncoded");
        check(ms.contains("getType"), "declares getType");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
