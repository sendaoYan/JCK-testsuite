/*
 * @test
 * @summary sun.nio.cs.ext.DoubleByte.Encoder_EBCDIC conformance: class and members.
 * @run main Encoder_EBCDICTest
 */

public class Encoder_EBCDICTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.DoubleByte$Encoder_EBCDIC", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.DoubleByte$Encoder_EBCDIC"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.DoubleByte$Encoder"), "superclass sun.nio.cs.ext.DoubleByte$Encoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("encode"), "declares method encode");
        check(mnames.contains("encodeArrayLoop"), "declares method encodeArrayLoop");
        check(mnames.contains("encodeBufferLoop"), "declares method encodeBufferLoop");
        check(mnames.contains("implFlush"), "declares method implFlush");
        check(mnames.contains("implReset"), "declares method implReset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
