/*
 * @test
 * @summary sun.nio.cs.EUC_JP.Encoder / sun.nio.cs.ext.EUC_JP.Encoder conformance: internal charset class; package is build-dependent (probed in both).
 * @modules java.base/sun.nio.cs:+open jdk.charsets/sun.nio.cs.ext:+open
 * @run main EncoderTest
 */
public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    
    static Class<?> load(String base) throws Exception {
        for (String pkg : new String[]{"sun.nio.cs.", "sun.nio.cs.ext."}) {
            try { return Class.forName(pkg + base, false, EncoderTest.class.getClassLoader()); }
            catch (ClassNotFoundException ignored) { }
        }
        throw new ClassNotFoundException("sun.nio.cs." + base + " / sun.nio.cs.ext." + base);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = load("EUC_JP$Encoder");
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.CharsetEncoder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canEncode"), "method canEncode");
        check(hasMethod(c, "encodeDouble"), "method encodeDouble");
        check(hasMethod(c, "encodeLoop"), "method encodeLoop");
        check(hasMethod(c, "encodeSingle"), "method encodeSingle");
        check(hasField(c, "ENC0201"), "field ENC0201");
        check(hasField(c, "ENC0208"), "field ENC0208");
        check(hasField(c, "ENC0212"), "field ENC0212");
        check(hasField(c, "enc0201"), "field enc0201");
        check(hasField(c, "enc0208"), "field enc0208");
        check(hasField(c, "enc0212"), "field enc0212");
        check(hasField(c, "sgp"), "field sgp");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
