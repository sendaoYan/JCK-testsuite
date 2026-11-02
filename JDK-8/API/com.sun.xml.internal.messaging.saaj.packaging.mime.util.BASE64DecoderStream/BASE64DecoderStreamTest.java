/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream conformance: class hierarchy and members.
 * @run main BASE64DecoderStreamTest
 */

public class BASE64DecoderStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.util.BASE64DecoderStream"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.FilterInputStream".equals(c.getSuperclass().getName()), "extends java.io.FilterInputStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("available"), "has method available");
        check(mn.contains("decode"), "has method decode");
        check(mn.contains("markSupported"), "has method markSupported");
        check(mn.contains("read"), "has method read");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
