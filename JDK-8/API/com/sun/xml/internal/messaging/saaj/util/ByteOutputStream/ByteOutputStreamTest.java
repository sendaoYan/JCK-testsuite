/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.ByteOutputStream conformance: class hierarchy and members.
 * @run main ByteOutputStreamTest
 */

public class ByteOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.ByteOutputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.ByteOutputStream"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.OutputStream".equals(c.getSuperclass().getName()), "extends java.io.OutputStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("ensureCapacity"), "has method ensureCapacity");
        check(mn.contains("getBytes"), "has method getBytes");
        check(mn.contains("getCount"), "has method getCount");
        check(mn.contains("newInputStream"), "has method newInputStream");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("size"), "has method size");
        check(mn.contains("toByteArray"), "has method toByteArray");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("write"), "has method write");
        check(mn.contains("writeAsAscii"), "has method writeAsAscii");
        check(mn.contains("writeTo"), "has method writeTo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
