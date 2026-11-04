/*
 * @test
 * @summary com.sun.xml.internal.stream.StaxErrorReporter conformance: class hierarchy and members.
 * @run main StaxErrorReporterTest
 */

public class StaxErrorReporterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.StaxErrorReporter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.StaxErrorReporter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.org.apache.xerces.internal.impl.XMLErrorReporter".equals(c.getSuperclass().getName()), "extends com.sun.org.apache.xerces.internal.impl.XMLErrorReporter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertToStaxLocation"), "has method convertToStaxLocation");
        check(mn.contains("reportError"), "has method reportError");
        check(mn.contains("reset"), "has method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
