/*
 * @test
 * @summary jdk.internal.foreign.abi.aarch64.CallArranger conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.foreign.abi.aarch64:+open
 * @run main CallArrangerTest
 */
public class CallArrangerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.aarch64.CallArranger", false, CallArrangerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.aarch64.CallArranger"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 12, "declared methods >= 12");
        check(hasM(ms, "abiDescriptor"), "method abiDescriptor");
        check(hasM(ms, "arrangeDowncall"), "method arrangeDowncall");
        check(hasM(ms, "arrangeUpcall"), "method arrangeUpcall");
        check(hasM(ms, "getArgumentClassForBindings"), "method getArgumentClassForBindings");
        check(hasM(ms, "getBindings"), "method getBindings");
        check(hasM(ms, "requiresSubSlotStackPacking"), "method requiresSubSlotStackPacking");
        check(hasM(ms, "spillsVariadicStructsPartially"), "method spillsVariadicStructsPartially");
        check(hasM(ms, "useIntRegsForVariadicFloatingPointArgs"), "method useIntRegsForVariadicFloatingPointArgs");
        check(hasM(ms, "varArgsOnStack"), "method varArgsOnStack");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 8, "declared fields >= 8");
        check(hasF(fs, "C"), "field C");
        check(hasF(fs, "LINUX"), "field LINUX");
        check(hasF(fs, "MACOS"), "field MACOS");
        check(hasF(fs, "MAX_REGISTER_ARGUMENTS"), "field MAX_REGISTER_ARGUMENTS");
        check(hasF(fs, "WINDOWS"), "field WINDOWS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
