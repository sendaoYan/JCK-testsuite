/*
 * @test
 * @summary sun.nio.cs.ext.SJIS_0213.Decoder conformance: class and members.
 * @run main DecoderTest
 */

public class DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.SJIS_0213$Decoder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.SJIS_0213$Decoder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass java.nio.charset.CharsetDecoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("decodeArrayLoop"), "declares method decodeArrayLoop");
        check(mnames.contains("decodeBufferLoop"), "declares method decodeBufferLoop");
        check(mnames.contains("decodeDouble"), "declares method decodeDouble");
        check(mnames.contains("decodeDoubleEx"), "declares method decodeDoubleEx");
        check(mnames.contains("decodeLoop"), "declares method decodeLoop");
        check(mnames.contains("decodeSingle"), "declares method decodeSingle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
