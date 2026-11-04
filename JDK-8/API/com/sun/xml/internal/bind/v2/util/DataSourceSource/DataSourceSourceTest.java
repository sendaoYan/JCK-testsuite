/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.util.DataSourceSource conformance: class hierarchy and members.
 * @run main DataSourceSourceTest
 */

public class DataSourceSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.util.DataSourceSource", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.util.DataSourceSource"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.transform.stream.StreamSource".equals(c.getSuperclass().getName()), "extends javax.xml.transform.stream.StreamSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDataSource"), "has method getDataSource");
        check(mn.contains("getInputStream"), "has method getInputStream");
        check(mn.contains("getReader"), "has method getReader");
        check(mn.contains("setInputStream"), "has method setInputStream");
        check(mn.contains("setReader"), "has method setReader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
