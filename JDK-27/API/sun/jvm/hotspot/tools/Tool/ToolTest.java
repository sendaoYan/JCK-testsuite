/*
 * @test
 * @summary sun.jvm.hotspot.tools.Tool conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.tools:+open
 * @run main ToolTest
 */
import java.lang.reflect.*;

public class ToolTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.tools.Tool", false, ToolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Runnable"), "interface java.lang.Runnable");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "start"), "method start");
        check(hasMethod(c, "execute"), "method execute");
        check(hasMethod(c, "stop"), "method stop");
        check(hasMethod(c, "getAgent"), "method getAgent");
        check(hasMethod(c, "usage"), "method usage");
        check(hasMethod(c, "needsJavaPrefix"), "method needsJavaPrefix");
        check(hasMethod(c, "printFlagsUsage"), "method printFlagsUsage");
        check(hasMethod(c, "printUsage"), "method printUsage");
        check(hasMethod(c, "setAgent"), "method setAgent");
        check(hasMethod(c, "setDebugeeType"), "method setDebugeeType");
        check(hasMethod(c, "getDebugeeType"), "method getDebugeeType");
        check(hasField(c, "DEBUGEE_PID"), "field DEBUGEE_PID");
        check(hasField(c, "DEBUGEE_CORE"), "field DEBUGEE_CORE");
        check(hasField(c, "DEBUGEE_REMOTE"), "field DEBUGEE_REMOTE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
