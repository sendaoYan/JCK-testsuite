/*
 * @test
 * @summary sun.nio.cs.ext.DoubleByteEncoder conformance: abstract class and members.
 * @run main DoubleByteEncoderTest
 */

public class DoubleByteEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.DoubleByteEncoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.DoubleByteEncoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass java.nio.charset.CharsetEncoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canEncode"), "declares method canEncode");
        check(mnames.contains("encodeArrayLoop"), "declares method encodeArrayLoop");
        check(mnames.contains("encodeBufferLoop"), "declares method encodeBufferLoop");
        check(mnames.contains("encodeDouble"), "declares method encodeDouble");
        check(mnames.contains("encodeLoop"), "declares method encodeLoop");
        check(mnames.contains("encodeSingle"), "declares method encodeSingle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
