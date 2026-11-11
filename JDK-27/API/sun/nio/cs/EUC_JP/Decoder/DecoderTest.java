/*
 * @test
 * @summary sun.nio.cs.EUC_JP.Decoder / sun.nio.cs.ext.EUC_JP.Decoder conformance: internal charset class; package is build-dependent (probed in both).
 * @modules java.base/sun.nio.cs:+open jdk.charsets/sun.nio.cs.ext:+open
 * @run main DecoderTest
 */
public class DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    
    static Class<?> load(String base) throws Exception {
        for (String pkg : new String[]{"sun.nio.cs.", "sun.nio.cs.ext."}) {
            try { return Class.forName(pkg + base, false, DecoderTest.class.getClassLoader()); }
            catch (ClassNotFoundException ignored) { }
        }
        throw new ClassNotFoundException("sun.nio.cs." + base + " / sun.nio.cs.ext." + base);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = load("EUC_JP$Decoder");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.nio.cs.DelegatableDecoder"), "interface sun.nio.cs.DelegatableDecoder");
        check(hasMethod(c, "decodeDouble"), "method decodeDouble");
        check(hasMethod(c, "decodeLoop"), "method decodeLoop");
        check(hasMethod(c, "implFlush"), "method implFlush");
        check(hasMethod(c, "implReset"), "method implReset");
        check(hasField(c, "DEC0201"), "field DEC0201");
        check(hasField(c, "DEC0208"), "field DEC0208");
        check(hasField(c, "DEC0212"), "field DEC0212");
        check(hasField(c, "dec0201"), "field dec0201");
        check(hasField(c, "dec0208"), "field dec0208");
        check(hasField(c, "dec0212"), "field dec0212");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
