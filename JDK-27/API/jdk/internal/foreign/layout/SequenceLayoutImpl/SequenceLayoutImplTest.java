/*
 * @test
 * @summary jdk.internal.foreign.layout.SequenceLayoutImpl conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.layout:+open
 * @run main SequenceLayoutImplTest
 */
public class SequenceLayoutImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.layout.SequenceLayoutImpl", false, SequenceLayoutImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.layout.SequenceLayoutImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.foreign.layout.AbstractLayout"), "superclass jdk.internal.foreign.layout.AbstractLayout");
        check(c.getInterfaces().length >= 1, "direct interfaces >= 1");
        check(hasI(c.getInterfaces(), "java.lang.foreign.SequenceLayout"), "interface java.lang.foreign.SequenceLayout");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 20, "declared methods >= 20");
        check(hasM(ms, "elementCount"), "method elementCount");
        check(hasM(ms, "elementLayout"), "method elementLayout");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "flatten"), "method flatten");
        check(hasM(ms, "hasNaturalAlignment"), "method hasNaturalAlignment");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "of"), "method of");
        check(hasM(ms, "reshape"), "method reshape");
        check(hasM(ms, "toString"), "method toString");
        check(hasM(ms, "withByteAlignment"), "method withByteAlignment");
        check(hasM(ms, "withElementCount"), "method withElementCount");
        check(hasM(ms, "withName"), "method withName");
        check(hasM(ms, "withoutName"), "method withoutName");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
