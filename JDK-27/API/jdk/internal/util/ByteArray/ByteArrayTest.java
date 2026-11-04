/*
 * @test
 * @summary jdk.internal.util.ByteArray conformance: internal class reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main ByteArrayTest
 */
public class ByteArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.ByteArray", false, ByteArrayTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.ByteArray"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 21, "declared methods >= 21");
        check(hasM(ms, "getBoolean"), "method getBoolean");
        check(hasM(ms, "getChar"), "method getChar");
        check(hasM(ms, "getDouble"), "method getDouble");
        check(hasM(ms, "getDoubleRaw"), "method getDoubleRaw");
        check(hasM(ms, "getFloat"), "method getFloat");
        check(hasM(ms, "getFloatRaw"), "method getFloatRaw");
        check(hasM(ms, "getInt"), "method getInt");
        check(hasM(ms, "getLong"), "method getLong");
        check(hasM(ms, "getShort"), "method getShort");
        check(hasM(ms, "getUnsignedShort"), "method getUnsignedShort");
        check(hasM(ms, "setBoolean"), "method setBoolean");
        check(hasM(ms, "setChar"), "method setChar");
        check(hasM(ms, "setDouble"), "method setDouble");
        check(hasM(ms, "setDoubleRaw"), "method setDoubleRaw");
        check(hasM(ms, "setFloat"), "method setFloat");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 6, "declared fields >= 6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
