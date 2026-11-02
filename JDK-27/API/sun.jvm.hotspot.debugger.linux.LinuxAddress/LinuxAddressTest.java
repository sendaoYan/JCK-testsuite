/*
 * @test
 * @summary sun.jvm.hotspot.debugger.linux.LinuxAddress conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.linux:+open
 * @run main LinuxAddressTest
 */
import java.lang.reflect.*;

public class LinuxAddressTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.linux.LinuxAddress", false, LinuxAddressTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.Address"), "interface sun.jvm.hotspot.debugger.Address");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "minus"), "method minus");
        check(hasMethod(c, "addOffsetTo"), "method addOffsetTo");
        check(hasMethod(c, "getCIntegerAt"), "method getCIntegerAt");
        check(hasMethod(c, "getAddressAt"), "method getAddressAt");
        check(hasMethod(c, "getCompOopAddressAt"), "method getCompOopAddressAt");
        check(hasMethod(c, "getCompKlassAddressAt"), "method getCompKlassAddressAt");
        check(hasMethod(c, "getJBooleanAt"), "method getJBooleanAt");
        check(hasMethod(c, "getJByteAt"), "method getJByteAt");
        check(hasMethod(c, "getJCharAt"), "method getJCharAt");
        check(hasMethod(c, "getJDoubleAt"), "method getJDoubleAt");
        check(hasMethod(c, "getJFloatAt"), "method getJFloatAt");
        check(hasMethod(c, "getJIntAt"), "method getJIntAt");
        check(hasMethod(c, "getJLongAt"), "method getJLongAt");
        check(hasMethod(c, "getJShortAt"), "method getJShortAt");
        check(hasMethod(c, "getOopHandleAt"), "method getOopHandleAt");
        check(hasMethod(c, "getCompOopHandleAt"), "method getCompOopHandleAt");
        check(hasMethod(c, "setCIntegerAt"), "method setCIntegerAt");
        check(hasMethod(c, "setAddressAt"), "method setAddressAt");
        check(hasMethod(c, "setJBooleanAt"), "method setJBooleanAt");
        check(hasMethod(c, "setJByteAt"), "method setJByteAt");
        check(hasMethod(c, "setJCharAt"), "method setJCharAt");
        check(hasMethod(c, "setJDoubleAt"), "method setJDoubleAt");
        check(hasMethod(c, "setJFloatAt"), "method setJFloatAt");
        check(hasMethod(c, "setJIntAt"), "method setJIntAt");
        check(hasMethod(c, "setJLongAt"), "method setJLongAt");
        check(hasMethod(c, "setJShortAt"), "method setJShortAt");
        check(hasMethod(c, "setOopHandleAt"), "method setOopHandleAt");
        check(hasMethod(c, "addOffsetToAsOopHandle"), "method addOffsetToAsOopHandle");
        check(hasMethod(c, "lessThan"), "method lessThan");
        check(hasMethod(c, "lessThanOrEqual"), "method lessThanOrEqual");
        check(hasMethod(c, "greaterThan"), "method greaterThan");
        check(hasMethod(c, "greaterThanOrEqual"), "method greaterThanOrEqual");
        check(hasMethod(c, "andWithMask"), "method andWithMask");
        check(hasMethod(c, "orWithMask"), "method orWithMask");
        check(hasMethod(c, "xorWithMask"), "method xorWithMask");
        check(hasMethod(c, "asLongValue"), "method asLongValue");
        check(hasField(c, "debugger"), "field debugger");
        check(hasField(c, "addr"), "field addr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
