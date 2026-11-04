/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicDebugEvent conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicDebugEventTest
 */
import java.lang.reflect.*;

public class BasicDebugEventTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicDebugEvent", false, BasicDebugEventTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.DebugEvent"), "interface sun.jvm.hotspot.debugger.cdbg.DebugEvent");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getAddress"), "method getAddress");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "getPC"), "method getPC");
        check(hasMethod(c, "getThread"), "method getThread");
        check(hasMethod(c, "setPC"), "method setPC");
        check(hasMethod(c, "getWasWrite"), "method getWasWrite");
        check(hasMethod(c, "getUnknownEventDetail"), "method getUnknownEventDetail");
        check(hasMethod(c, "setWasWrite"), "method setWasWrite");
        check(hasMethod(c, "setAddress"), "method setAddress");
        check(hasMethod(c, "setUnknownEventDetail"), "method setUnknownEventDetail");
        check(hasMethod(c, "setThread"), "method setThread");
        check(hasMethod(c, "newLoadObjectLoadEvent"), "method newLoadObjectLoadEvent");
        check(hasMethod(c, "newLoadObjectUnloadEvent"), "method newLoadObjectUnloadEvent");
        check(hasMethod(c, "newBreakpointEvent"), "method newBreakpointEvent");
        check(hasMethod(c, "newSingleStepEvent"), "method newSingleStepEvent");
        check(hasMethod(c, "newAccessViolationEvent"), "method newAccessViolationEvent");
        check(hasMethod(c, "newUnknownEvent"), "method newUnknownEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
