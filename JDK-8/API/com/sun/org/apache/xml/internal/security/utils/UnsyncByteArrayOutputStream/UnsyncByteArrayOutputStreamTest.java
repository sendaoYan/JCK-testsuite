/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.UnsyncByteArrayOutputStream conformance: class hierarchy and members.
 * @run main UnsyncByteArrayOutputStreamTest
 */
public class UnsyncByteArrayOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.UnsyncByteArrayOutputStream", false, UnsyncByteArrayOutputStreamTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass java.io.OutputStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("write"), "method write");
        check(mn.contains("toByteArray"), "method toByteArray");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("writeTo"), "method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
