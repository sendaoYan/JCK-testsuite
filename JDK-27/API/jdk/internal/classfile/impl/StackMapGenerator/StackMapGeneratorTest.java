/*
 * @test
 * @summary jdk.internal.classfile.impl.StackMapGenerator conformance: internal class reflection.
 * @modules java.base/jdk.internal.classfile.impl:+open
 * @run main StackMapGeneratorTest
 */
public class StackMapGeneratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.classfile.impl.StackMapGenerator", false, StackMapGeneratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.classfile.impl.StackMapGenerator"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 25, "declared methods >= 25");
        check(hasM(ms, "maxLocals"), "method maxLocals");
        check(hasM(ms, "maxStack"), "method maxStack");
        check(hasM(ms, "stackMapTableAttribute"), "method stackMapTableAttribute");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 52, "declared fields >= 52");
        check(hasF(fs, "APPEND_FRAME_END"), "field APPEND_FRAME_END");
        check(hasF(fs, "APPEND_FRAME_START"), "field APPEND_FRAME_START");
        check(hasF(fs, "CHOP_FRAME_END"), "field CHOP_FRAME_END");
        check(hasF(fs, "CHOP_FRAME_START"), "field CHOP_FRAME_START");
        check(hasF(fs, "FULL_FRAME"), "field FULL_FRAME");
        check(hasF(fs, "ITEM_BOGUS"), "field ITEM_BOGUS");
        check(hasF(fs, "ITEM_BOOLEAN"), "field ITEM_BOOLEAN");
        check(hasF(fs, "ITEM_BYTE"), "field ITEM_BYTE");
        check(hasF(fs, "ITEM_CHAR"), "field ITEM_CHAR");
        check(hasF(fs, "ITEM_DOUBLE"), "field ITEM_DOUBLE");
        check(hasF(fs, "ITEM_DOUBLE_2ND"), "field ITEM_DOUBLE_2ND");
        check(hasF(fs, "ITEM_FLOAT"), "field ITEM_FLOAT");
        check(hasF(fs, "ITEM_INTEGER"), "field ITEM_INTEGER");
        check(hasF(fs, "ITEM_LONG"), "field ITEM_LONG");
        check(hasF(fs, "ITEM_LONG_2ND"), "field ITEM_LONG_2ND");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
