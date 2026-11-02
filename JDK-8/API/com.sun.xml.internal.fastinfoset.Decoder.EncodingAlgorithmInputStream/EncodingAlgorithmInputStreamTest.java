/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.Decoder.EncodingAlgorithmInputStream conformance: class hierarchy and members.
 * @run main EncodingAlgorithmInputStreamTest
 */

public class EncodingAlgorithmInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.Decoder$EncodingAlgorithmInputStream", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.Decoder$EncodingAlgorithmInputStream"), "binary name");
        check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is not public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.io.InputStream".equals(c.getSuperclass().getName()), "extends java.io.InputStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("read"), "has method read");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
