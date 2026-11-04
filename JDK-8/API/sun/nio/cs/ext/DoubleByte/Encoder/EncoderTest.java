/*
 * @test
 * @summary sun.nio.cs.ext.DoubleByte.Encoder conformance: class and members.
 * @run main EncoderTest
 */

public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.DoubleByte$Encoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.DoubleByte$Encoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass java.nio.charset.CharsetEncoder");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.nio.cs.ArrayEncoder"), "interface sun.nio.cs.ArrayEncoder");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canEncode"), "declares method canEncode");
        check(mnames.contains("encode"), "declares method encode");
        check(mnames.contains("encodeArrayLoop"), "declares method encodeArrayLoop");
        check(mnames.contains("encodeBufferLoop"), "declares method encodeBufferLoop");
        check(mnames.contains("encodeChar"), "declares method encodeChar");
        check(mnames.contains("encodeLoop"), "declares method encodeLoop");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
