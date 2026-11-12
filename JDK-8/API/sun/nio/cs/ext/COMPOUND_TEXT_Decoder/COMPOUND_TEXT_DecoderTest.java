/*
 * @test
 * @summary sun.nio.cs.ext.COMPOUND_TEXT_Decoder / sun.nio.cs.COMPOUND_TEXT_Decoder conformance: internal charset class; package is build-dependent (probed in both).
 * @run main COMPOUND_TEXT_DecoderTest
 */

public class COMPOUND_TEXT_DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    
    static Class<?> load(String base) throws Exception {
        for (String pkg : new String[]{"sun.nio.cs.", "sun.nio.cs.ext."}) {
            try { return Class.forName(pkg + base, false, ClassLoader.getSystemClassLoader()); }
            catch (ClassNotFoundException ignored) { }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = load("COMPOUND_TEXT_Decoder");
        if (c == null) {
            System.out.println("PASS (SKIP: COMPOUND_TEXT charset absent on this platform)");
            return;
        }
        check((c.getName().equals("sun.nio.cs.ext.COMPOUND_TEXT_Decoder") || c.getName().equals("sun.nio.cs.COMPOUND_TEXT_Decoder")), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass java.nio.charset.CharsetDecoder");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("charset9496L"), "declares method charset9496L");
        check(mnames.contains("charset9496R"), "declares method charset9496R");
        check(mnames.contains("charset94N"), "declares method charset94N");
        check(mnames.contains("charset94NL"), "declares method charset94NL");
        check(mnames.contains("charset94NR"), "declares method charset94NR");
        check(mnames.contains("charsetNonStandard"), "declares method charsetNonStandard");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
