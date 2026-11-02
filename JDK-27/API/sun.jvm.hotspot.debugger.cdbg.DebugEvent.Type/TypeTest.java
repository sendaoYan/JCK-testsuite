/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.DebugEvent.Type conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg:+open
 * @run main TypeTest
 */
import java.lang.reflect.*;

public class TypeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.DebugEvent$Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "LOADOBJECT_LOAD"), "field LOADOBJECT_LOAD");
        check(hasField(c, "LOADOBJECT_UNLOAD"), "field LOADOBJECT_UNLOAD");
        check(hasField(c, "BREAKPOINT"), "field BREAKPOINT");
        check(hasField(c, "SINGLE_STEP"), "field SINGLE_STEP");
        check(hasField(c, "ACCESS_VIOLATION"), "field ACCESS_VIOLATION");
        check(hasField(c, "UNKNOWN"), "field UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
