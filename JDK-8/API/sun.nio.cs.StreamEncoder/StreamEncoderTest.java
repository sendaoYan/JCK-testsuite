/*
 * @test
 * @summary sun.nio.cs.StreamEncoder conformance: class and members.
 * @run main StreamEncoderTest
 */

public class StreamEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.StreamEncoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.StreamEncoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.Writer"), "superclass java.io.Writer");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("encodingName"), "declares method encodingName");
        check(mnames.contains("ensureOpen"), "declares method ensureOpen");
        check(mnames.contains("flush"), "declares method flush");
        check(mnames.contains("flushBuffer"), "declares method flushBuffer");
        check(mnames.contains("flushLeftoverChar"), "declares method flushLeftoverChar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
