/*
 * @test
 * @summary com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmIndexes conformance: class hierarchy and members.
 * @run main EncodingAlgorithmIndexesTest
 */

public class EncodingAlgorithmIndexesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmIndexes", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.org.jvnet.fastinfoset.EncodingAlgorithmIndexes"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
