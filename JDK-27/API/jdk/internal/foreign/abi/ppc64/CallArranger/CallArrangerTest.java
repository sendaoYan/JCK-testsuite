/*
 * @test
 * @summary jdk.internal.foreign.abi.ppc64.CallArranger conformance: internal abstract class reflection.
 * @modules java.base/jdk.internal.foreign.abi.ppc64:+open
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
        Class<?> c = Class.forName("jdk.internal.foreign.abi.ppc64.CallArranger", false, CallArrangerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.ppc64.CallArranger"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 8, "declared methods >= 8");
        check(hasM(ms, "arrangeDowncall"), "method arrangeDowncall");
        check(hasM(ms, "arrangeUpcall"), "method arrangeUpcall");
        check(hasM(ms, "getBindings"), "method getBindings");
        check(hasM(ms, "isAIX"), "method isAIX");
        check(hasM(ms, "useABIv2"), "method useABIv2");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 10, "declared fields >= 10");
        check(hasF(fs, "ABIv1"), "field ABIv1");
        check(hasF(fs, "ABIv2"), "field ABIv2");
        check(hasF(fs, "AIX"), "field AIX");
        check(hasF(fs, "MAX_FLOAT_REGISTER_ARGUMENTS"), "field MAX_FLOAT_REGISTER_ARGUMENTS");
        check(hasF(fs, "MAX_REGISTER_ARGUMENTS"), "field MAX_REGISTER_ARGUMENTS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
