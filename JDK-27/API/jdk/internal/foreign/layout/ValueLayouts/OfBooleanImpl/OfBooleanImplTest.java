/*
 * @test
 * @summary jdk.internal.foreign.layout.ValueLayouts.OfBooleanImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.layout:+open
 * @run main OfBooleanImplTest
 */
public class OfBooleanImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.layout.ValueLayouts$OfBooleanImpl", false, OfBooleanImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.layout.ValueLayouts$OfBooleanImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.foreign.layout.ValueLayouts$AbstractValueLayout"), "superclass jdk.internal.foreign.layout.ValueLayouts$AbstractValueLayout");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.foreign.ValueLayout$OfBoolean"), "interface java.lang.foreign.ValueLayout$OfBoolean");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 17, "declared methods >= 17");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "of"), "method of");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "withByteAlignment"), "method withByteAlignment");
        check(hasM(ms, "withName"), "method withName");
        check(hasM(ms, "withOrder"), "method withOrder");
        check(hasM(ms, "withoutName"), "method withoutName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
