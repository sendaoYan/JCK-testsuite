/*
 * @test
 * @summary com.sun.istack.internal.ByteArrayDataSource conformance: hierarchy and declared members via reflection.
 * @run main ByteArrayDataSourceTest
 */

public class ByteArrayDataSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.istack.internal.ByteArrayDataSource", false, null);
        check(c.getName().equals("com.sun.istack.internal.ByteArrayDataSource"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.activation.DataSource"), "implements javax.activation.DataSource");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getContentType"), "declares getContentType");
        check(ms.contains("getInputStream"), "declares getInputStream");
        check(ms.contains("getName"), "declares getName");
        check(ms.contains("getOutputStream"), "declares getOutputStream");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
