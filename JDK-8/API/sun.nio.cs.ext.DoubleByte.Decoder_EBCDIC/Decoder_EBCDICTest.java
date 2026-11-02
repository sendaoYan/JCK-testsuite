/*
 * @test
 * @summary sun.nio.cs.ext.DoubleByte.Decoder_EBCDIC conformance: class and members.
 * @run main Decoder_EBCDICTest
 */

public class Decoder_EBCDICTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.DoubleByte$Decoder_EBCDIC", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.DoubleByte$Decoder_EBCDIC"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.cs.ext.DoubleByte$Decoder"), "superclass sun.nio.cs.ext.DoubleByte$Decoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("decode"), "declares method decode");
        check(mnames.contains("decodeArrayLoop"), "declares method decodeArrayLoop");
        check(mnames.contains("decodeBufferLoop"), "declares method decodeBufferLoop");
        check(mnames.contains("implReset"), "declares method implReset");
        check(mnames.contains("isDoubleByte"), "declares method isDoubleByte");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
