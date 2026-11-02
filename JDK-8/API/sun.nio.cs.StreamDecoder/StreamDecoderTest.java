/*
 * @test
 * @summary sun.nio.cs.StreamDecoder conformance: class and members.
 * @run main StreamDecoderTest
 */

public class StreamDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.StreamDecoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.StreamDecoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.Reader"), "superclass java.io.Reader");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("encodingName"), "declares method encodingName");
        check(mnames.contains("ensureOpen"), "declares method ensureOpen");
        check(mnames.contains("forDecoder"), "declares method forDecoder");
        check(mnames.contains("forInputStreamReader"), "declares method forInputStreamReader");
        check(mnames.contains("getChannel"), "declares method getChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
