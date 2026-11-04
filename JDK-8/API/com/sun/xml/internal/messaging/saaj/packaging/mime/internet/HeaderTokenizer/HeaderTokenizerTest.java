/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.internet.HeaderTokenizer conformance: class hierarchy and members.
 * @run main HeaderTokenizerTest
 */

public class HeaderTokenizerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.HeaderTokenizer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.internet.HeaderTokenizer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("filterToken"), "has method filterToken");
        check(mn.contains("getNext"), "has method getNext");
        check(mn.contains("getRemainder"), "has method getRemainder");
        check(mn.contains("next"), "has method next");
        check(mn.contains("peek"), "has method peek");
        check(mn.contains("skipWhiteSpace"), "has method skipWhiteSpace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
