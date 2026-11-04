/*
 * @test
 * @summary com.sun.xml.internal.stream.writers.UTF8OutputStreamWriter conformance: class hierarchy and members.
 * @run main UTF8OutputStreamWriterTest
 */

public class UTF8OutputStreamWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.writers.UTF8OutputStreamWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.writers.UTF8OutputStreamWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.Writer".equals(c.getSuperclass().getName()), "extends java.io.Writer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "has method close");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("getEncoding"), "has method getEncoding");
        check(mn.contains("write"), "has method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
