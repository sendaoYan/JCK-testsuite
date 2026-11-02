/*
 * @test
 * @summary jdk.internal.perf.PerfCounter conformance: internal package reflection.
 * @modules java.base/jdk.internal.perf:+open
 * @run main PerfCounterTest
 */
public class PerfCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.perf.PerfCounter", false, PerfCounterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addElapsedTimeFrom"), "method addElapsedTimeFrom");
        check(hasMethod(c, "addTime"), "method addTime");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "getFindClassTime"), "method getFindClassTime");
        check(hasMethod(c, "getFindClasses"), "method getFindClasses");
        check(hasMethod(c, "getParentDelegationTime"), "method getParentDelegationTime");
        check(hasMethod(c, "getReadClassBytesTime"), "method getReadClassBytesTime");
        check(hasMethod(c, "getZipFileCount"), "method getZipFileCount");
        check(hasMethod(c, "getZipFileOpenTime"), "method getZipFileOpenTime");
        check(hasMethod(c, "increment"), "method increment");
        check(hasMethod(c, "newConstantPerfCounter"), "method newConstantPerfCounter");
        check(hasMethod(c, "newPerfCounter"), "method newPerfCounter");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "U_None"), "field U_None");
        check(hasField(c, "V_Constant"), "field V_Constant");
        check(hasField(c, "V_Monotonic"), "field V_Monotonic");
        check(hasField(c, "V_Variable"), "field V_Variable");
        check(hasField(c, "lb"), "field lb");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "perf"), "field perf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
