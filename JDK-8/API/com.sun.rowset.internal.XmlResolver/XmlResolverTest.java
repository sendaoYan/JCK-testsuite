/*
 * @test
 * @summary com.sun.rowset.internal.XmlResolver conformance: hierarchy and declared members via reflection.
 * @run main XmlResolverTest
 */

public class XmlResolverTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.internal.XmlResolver", false, null);
        check(c.getName().equals("com.sun.rowset.internal.XmlResolver"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.xml.sax.EntityResolver"), "implements org.xml.sax.EntityResolver");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("resolveEntity"), "declares resolveEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
