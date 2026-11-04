/*
 * @test
 * @summary sun.nio.cs.ext.IBM834.Encoder conformance: class and members.
 * @run main EncoderTest
 */

public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.IBM834$Encoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.IBM834$Encoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.DoubleByte$Encoder_DBCSONLY"), "superclass sun.nio.cs.ext.DoubleByte$Encoder_DBCSONLY");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("encodeChar"), "declares method encodeChar");
        check(mnames.contains("isLegalReplacement"), "declares method isLegalReplacement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
