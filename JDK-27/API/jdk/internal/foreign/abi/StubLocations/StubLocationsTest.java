/*
 * @test
 * @summary jdk.internal.foreign.abi.StubLocations conformance: internal enum reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main StubLocationsTest
 */
public class StubLocationsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.StubLocations", false, StubLocationsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.StubLocations"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 4, "declared methods >= 4");
        check(hasM(ms, "storage"), "method storage");
        check(hasM(ms, "valueOf"), "method valueOf");
        check(hasM(ms, "values"), "method values");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 4, "declared fields >= 4");
        check(hasF(fs, "CAPTURED_STATE_BUFFER"), "field CAPTURED_STATE_BUFFER");
        check(hasF(fs, "RETURN_BUFFER"), "field RETURN_BUFFER");
        check(hasF(fs, "TARGET_ADDRESS"), "field TARGET_ADDRESS");
        check(hasEnum(fs, "CAPTURED_STATE_BUFFER"), "enum constant CAPTURED_STATE_BUFFER");
        check(hasEnum(fs, "RETURN_BUFFER"), "enum constant RETURN_BUFFER");
        check(hasEnum(fs, "TARGET_ADDRESS"), "enum constant TARGET_ADDRESS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
