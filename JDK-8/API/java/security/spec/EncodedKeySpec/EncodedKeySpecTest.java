/*
 * @test
 * @summary java.security.spec.EncodedKeySpec conformance: abstract class hierarchy and key methods.
 * @run main EncodedKeySpecTest
 */

public class EncodedKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.spec.EncodedKeySpec");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.spec.KeySpec"), "implements java.security.spec.KeySpec");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getEncoded"), "declares getEncoded");
        check(ms.contains("getFormat"), "declares getFormat");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
