/*
 * @test
 * @summary sun.jvm.hotspot.utilities.StreamMonitor conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main StreamMonitorTest
 */
import java.lang.reflect.*;

public class StreamMonitorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.StreamMonitor", false, StreamMonitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Runnable"), "interface java.lang.Runnable");
        check(hasMethod(c, "run"), "method run");
        check(hasMethod(c, "waitFor"), "method waitFor");
        check(hasMethod(c, "addTrigger"), "method addTrigger");
        check(hasMethod(c, "removeTrigger"), "method removeTrigger");
        check(hasMethod(c, "getTriggersSeen"), "method getTriggersSeen");
        check(hasMethod(c, "startCapture"), "method startCapture");
        check(hasMethod(c, "stopCapture"), "method stopCapture");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
