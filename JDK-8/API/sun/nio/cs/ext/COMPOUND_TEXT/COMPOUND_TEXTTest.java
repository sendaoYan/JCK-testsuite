/*
 * @test
 * @summary sun.nio.cs.ext.COMPOUND_TEXT / sun.nio.cs.COMPOUND_TEXT conformance: internal charset class; package is build-dependent (probed in both).
 * @library /lib
 * @run main COMPOUND_TEXTTest
 */

public class COMPOUND_TEXTTest {
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
        Class<?> c = load("COMPOUND_TEXT");
        if (c == null) {
            throw new jtreg.SkippedException("COMPOUND_TEXT charset absent on this platform");
        }
        check((c.getName().equals("sun.nio.cs.ext.COMPOUND_TEXT") || c.getName().equals("sun.nio.cs.COMPOUND_TEXT")), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.Charset"), "superclass java.nio.charset.Charset");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("contains"), "declares method contains");
        check(mnames.contains("newDecoder"), "declares method newDecoder");
        check(mnames.contains("newEncoder"), "declares method newEncoder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
