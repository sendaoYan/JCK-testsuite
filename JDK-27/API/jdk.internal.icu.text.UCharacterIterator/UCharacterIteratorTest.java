/*
 * @test
 * @summary jdk.internal.icu.text.UCharacterIterator conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main UCharacterIteratorTest
 */
public class UCharacterIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.UCharacterIterator", false, UCharacterIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.UCharacterIterator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 17, "declared methods >= 17");
        check(hasM(ms, "clone"), "method clone");
        check(hasM(ms, "getIndex"), "method getIndex");
        check(hasM(ms, "getInstance"), "method getInstance");
        check(hasM(ms, "getLength"), "method getLength");
        check(hasM(ms, "getText"), "method getText");
        check(hasM(ms, "moveCodePointIndex"), "method moveCodePointIndex");
        check(hasM(ms, "next"), "method next");
        check(hasM(ms, "nextCodePoint"), "method nextCodePoint");
        check(hasM(ms, "previous"), "method previous");
        check(hasM(ms, "previousCodePoint"), "method previousCodePoint");
        check(hasM(ms, "setIndex"), "method setIndex");
        check(hasM(ms, "setToStart"), "method setToStart");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 1, "declared fields >= 1");
        check(hasF(fs, "DONE"), "field DONE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
