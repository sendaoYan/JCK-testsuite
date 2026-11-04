/*
 * @test
 * @summary jdk.internal.foreign.abi.LinkerOptions conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign.abi:+open
 * @run main LinkerOptionsTest
 */
public class LinkerOptionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.abi.LinkerOptions", false, LinkerOptionsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.abi.LinkerOptions"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 14, "declared methods >= 14");
        check(hasM(ms, "allowsHeapAccess"), "method allowsHeapAccess");
        check(hasM(ms, "capturedCallStateMask"), "method capturedCallStateMask");
        check(hasM(ms, "empty"), "method empty");
        check(hasM(ms, "equals"), "method equals");
        check(hasM(ms, "firstVariadicArgIndex"), "method firstVariadicArgIndex");
        check(hasM(ms, "forDowncall"), "method forDowncall");
        check(hasM(ms, "forUpcall"), "method forUpcall");
        check(hasM(ms, "hasCapturedCallState"), "method hasCapturedCallState");
        check(hasM(ms, "hashCode"), "method hashCode");
        check(hasM(ms, "isCritical"), "method isCritical");
        check(hasM(ms, "isVarargsIndex"), "method isVarargsIndex");
        check(hasM(ms, "isVariadicFunction"), "method isVariadicFunction");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 2, "declared fields >= 2");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
