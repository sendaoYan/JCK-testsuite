/*
 * @test
 * @summary jdk.internal.icu.impl.NormalizerImpl.ReorderingBuffer conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.impl:+open
 * @run main ReorderingBufferTest
 */
public class ReorderingBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.impl.NormalizerImpl$ReorderingBuffer", false, ReorderingBufferTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.impl.NormalizerImpl$ReorderingBuffer"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.Appendable"), "interface java.lang.Appendable");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 22, "declared methods >= 22");
        check(hasM(ms, "append"), "method append");
        check(hasM(ms, "appendZeroCC"), "method appendZeroCC");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "flush"), "method flush");
        check(hasM(ms, "flushAndAppendZeroCC"), "method flushAndAppendZeroCC");
        check(hasM(ms, "getLastCC"), "method getLastCC");
        check(hasM(ms, "getStringBuilder"), "method getStringBuilder");
        check(hasM(ms, "isEmpty"), "method isEmpty");
        check(hasM(ms, "length"), "method length");
        check(hasM(ms, "remove"), "method remove");
        check(hasM(ms, "removeSuffix"), "method removeSuffix");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 8, "declared fields >= 8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
