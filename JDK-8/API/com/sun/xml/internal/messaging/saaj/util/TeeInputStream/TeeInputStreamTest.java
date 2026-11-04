/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.TeeInputStream conformance: class hierarchy and members.
 * @run main TeeInputStreamTest
 */

public class TeeInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.TeeInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.TeeInputStream"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.InputStream".equals(c.getSuperclass().getName()), "extends java.io.InputStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("available"), "has method available");
        check(mn.contains("close"), "has method close");
        check(mn.contains("mark"), "has method mark");
        check(mn.contains("markSupported"), "has method markSupported");
        check(mn.contains("read"), "has method read");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("skip"), "has method skip");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
