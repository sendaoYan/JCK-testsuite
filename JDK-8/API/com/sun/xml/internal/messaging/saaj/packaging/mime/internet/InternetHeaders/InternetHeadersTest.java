/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.InternetHeaders conformance: class hierarchy and members.
 * @run main InternetHeadersTest
 */

public class InternetHeadersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.InternetHeaders", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.InternetHeaders"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addHeader"), "has method addHeader");
        check(mn.contains("addHeaderLine"), "has method addHeaderLine");
        check(mn.contains("getAllHeaderLines"), "has method getAllHeaderLines");
        check(mn.contains("getAllHeaders"), "has method getAllHeaders");
        check(mn.contains("getHeader"), "has method getHeader");
        check(mn.contains("load"), "has method load");
        check(mn.contains("removeHeader"), "has method removeHeader");
        check(mn.contains("setHeader"), "has method setHeader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
