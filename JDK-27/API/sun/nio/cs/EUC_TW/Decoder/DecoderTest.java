/*
 * @test
 * @summary sun.nio.cs.EUC_TW.Decoder / sun.nio.cs.ext.EUC_TW.Decoder conformance: internal charset class; package is build-dependent (probed in both).
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
        Class<?> c = load("EUC_TW$Decoder");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetDecoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "decodeLoop"), "method decodeLoop");
        check(hasMethod(c, "decodeSingleOrReplace"), "method decodeSingleOrReplace");
        check(hasMethod(c, "toUnicode"), "method toUnicode");
        check(hasField(c, "b1Max"), "field b1Max");
        check(hasField(c, "b1Min"), "field b1Min");
        check(hasField(c, "b2Max"), "field b2Max");
        check(hasField(c, "b2Min"), "field b2Min");
        check(hasField(c, "b2c"), "field b2c");
        check(hasField(c, "b2cIsSupp"), "field b2cIsSupp");
        check(hasField(c, "c1"), "field c1");
        check(hasField(c, "c2"), "field c2");
        check(hasField(c, "cnspToIndex"), "field cnspToIndex");
        check(hasField(c, "dbSegSize"), "field dbSegSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
