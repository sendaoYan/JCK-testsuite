/*
 * @test
 * @summary jdk.internal.foreign.SegmentFactories conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main SegmentFactoriesTest
 */
public class SegmentFactoriesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.SegmentFactories", false, SegmentFactoriesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.SegmentFactories"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 23, "declared methods >= 23");
        check(hasM(ms, "allocateNativeSegment"), "method allocateNativeSegment");
        check(hasM(ms, "arrayOfByteSegment"), "method arrayOfByteSegment");
        check(hasM(ms, "arrayOfCharSegment"), "method arrayOfCharSegment");
        check(hasM(ms, "arrayOfDoubleSegment"), "method arrayOfDoubleSegment");
        check(hasM(ms, "arrayOfFloatSegment"), "method arrayOfFloatSegment");
        check(hasM(ms, "arrayOfIntSegment"), "method arrayOfIntSegment");
        check(hasM(ms, "arrayOfLongSegment"), "method arrayOfLongSegment");
        check(hasM(ms, "arrayOfShortSegment"), "method arrayOfShortSegment");
        check(hasM(ms, "fromArray"), "method fromArray");
        check(hasM(ms, "makeNativeSegmentUnchecked"), "method makeNativeSegmentUnchecked");
        check(hasM(ms, "mapSegment"), "method mapSegment");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
