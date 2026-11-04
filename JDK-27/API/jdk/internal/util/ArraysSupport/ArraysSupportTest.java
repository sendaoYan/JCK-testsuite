/*
 * @test
 * @summary jdk.internal.util.ArraysSupport conformance: internal class reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main ArraysSupportTest
 */
public class ArraysSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.ArraysSupport", false, ArraysSupportTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.ArraysSupport"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 36, "declared methods >= 36");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "hashCodeOfUTF16"), "method hashCodeOfUTF16");
        check(hasM(ms, "hashCodeOfUnsigned"), "method hashCodeOfUnsigned");
        check(hasM(ms, "mismatch"), "method mismatch");
        check(hasM(ms, "newLength"), "method newLength");
        check(hasM(ms, "reverse"), "method reverse");
        check(hasM(ms, "toArrayReversed"), "method toArrayReversed");
        check(hasM(ms, "vectorizedMismatch"), "method vectorizedMismatch");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 21, "declared fields >= 21");
        check(hasF(fs, "LOG2_ARRAY_BOOLEAN_INDEX_SCALE"), "field LOG2_ARRAY_BOOLEAN_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_BYTE_INDEX_SCALE"), "field LOG2_ARRAY_BYTE_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_CHAR_INDEX_SCALE"), "field LOG2_ARRAY_CHAR_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_DOUBLE_INDEX_SCALE"), "field LOG2_ARRAY_DOUBLE_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_FLOAT_INDEX_SCALE"), "field LOG2_ARRAY_FLOAT_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_INT_INDEX_SCALE"), "field LOG2_ARRAY_INT_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_LONG_INDEX_SCALE"), "field LOG2_ARRAY_LONG_INDEX_SCALE");
        check(hasF(fs, "LOG2_ARRAY_SHORT_INDEX_SCALE"), "field LOG2_ARRAY_SHORT_INDEX_SCALE");
        check(hasF(fs, "SOFT_MAX_ARRAY_LENGTH"), "field SOFT_MAX_ARRAY_LENGTH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
