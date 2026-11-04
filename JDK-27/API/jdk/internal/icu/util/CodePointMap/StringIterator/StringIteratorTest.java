/*
 * @test
 * @summary jdk.internal.icu.util.CodePointMap.StringIterator conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.util:+open
 * @run main StringIteratorTest
 */
public class StringIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.util.CodePointMap$StringIterator", false, StringIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.util.CodePointMap$StringIterator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "getCodePoint"), "method getCodePoint");
        check(hasM(ms, "getIndex"), "method getIndex");
        check(hasM(ms, "getValue"), "method getValue");
        check(hasM(ms, "next"), "method next");
        check(hasM(ms, "previous"), "method previous");
        check(hasM(ms, "reset"), "method reset");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 5, "declared fields >= 5");
        check(hasF(fs, "c"), "field c");
        check(hasF(fs, "s"), "field s");
        check(hasF(fs, "sIndex"), "field sIndex");
        check(hasF(fs, "value"), "field value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
