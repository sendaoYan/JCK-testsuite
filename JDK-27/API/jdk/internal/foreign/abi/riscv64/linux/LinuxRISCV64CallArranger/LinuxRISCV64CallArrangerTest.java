/*
 * @test
 * @summary jdk.internal.foreign.abi.riscv64.linux.LinuxRISCV64CallArranger conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi.riscv64.linux:+open
 * @run main LinuxRISCV64CallArrangerTest
 */
public class LinuxRISCV64CallArrangerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.riscv64.linux.LinuxRISCV64CallArranger", false, LinuxRISCV64CallArrangerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.riscv64.linux.LinuxRISCV64CallArranger"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 6, "declared methods >= 6");
        check(hasM(ms, "arrangeDowncall"), "method arrangeDowncall");
        check(hasM(ms, "arrangeUpcall"), "method arrangeUpcall");
        check(hasM(ms, "getBindings"), "method getBindings");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 3, "declared fields >= 3");
        check(hasF(fs, "MAX_REGISTER_ARGUMENTS"), "field MAX_REGISTER_ARGUMENTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
