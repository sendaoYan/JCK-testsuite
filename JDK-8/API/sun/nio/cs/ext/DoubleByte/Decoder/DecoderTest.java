/*
 * @test
 * @summary sun.nio.cs.ext.DoubleByte.Decoder conformance: class and members.
 * @run main DecoderTest
 */

public class DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.DoubleByte$Decoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.DoubleByte$Decoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass java.nio.charset.CharsetDecoder");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.nio.cs.ext.DelegatableDecoder"), "interface sun.nio.cs.ext.DelegatableDecoder");
        check(ifaces.contains("sun.nio.cs.ArrayDecoder"), "interface sun.nio.cs.ArrayDecoder");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("crMalformedOrUnderFlow"), "declares method crMalformedOrUnderFlow");
        check(mnames.contains("crMalformedOrUnmappable"), "declares method crMalformedOrUnmappable");
        check(mnames.contains("decode"), "declares method decode");
        check(mnames.contains("decodeArrayLoop"), "declares method decodeArrayLoop");
        check(mnames.contains("decodeBufferLoop"), "declares method decodeBufferLoop");
        check(mnames.contains("decodeDouble"), "declares method decodeDouble");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
