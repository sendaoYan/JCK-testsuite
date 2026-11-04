/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.util.CharReader conformance: class hierarchy and members.
 * @run main CharReaderTest
 */

public class CharReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.util.CharReader", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.util.CharReader"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.CharArrayReader".equals(c.getSuperclass().getName()), "extends java.io.CharArrayReader");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getChars"), "has method getChars");
        check(mn.contains("getCount"), "has method getCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
