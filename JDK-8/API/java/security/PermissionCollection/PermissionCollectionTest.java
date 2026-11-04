/*
 * @test
 * @summary java.security.PermissionCollection conformance: abstract class hierarchy and key methods.
 * @run main PermissionCollectionTest
 */

public class PermissionCollectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.PermissionCollection");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("elements"), "declares elements");
        check(ms.contains("implies"), "declares implies");
        check(ms.contains("isReadOnly"), "declares isReadOnly");
        check(ms.contains("setReadOnly"), "declares setReadOnly");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
