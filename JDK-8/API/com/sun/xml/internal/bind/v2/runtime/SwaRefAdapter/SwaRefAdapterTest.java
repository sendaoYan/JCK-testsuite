/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.SwaRefAdapter conformance: class hierarchy and members.
 * @run main SwaRefAdapterTest
 */

public class SwaRefAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.SwaRefAdapter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.SwaRefAdapter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.bind.annotation.adapters.XmlAdapter".equals(c.getSuperclass().getName()), "extends javax.xml.bind.annotation.adapters.XmlAdapter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("marshal"), "has method marshal");
        check(mn.contains("unmarshal"), "has method unmarshal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
