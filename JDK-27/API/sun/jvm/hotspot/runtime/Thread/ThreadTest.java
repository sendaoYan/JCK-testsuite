/*
 * @test
 * @summary sun.jvm.hotspot.runtime.Thread conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main ThreadTest
 */
import java.lang.reflect.*;

public class ThreadTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.Thread", false, ThreadTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isLockOwned"), "method isLockOwned");
        check(hasMethod(c, "oopsDo"), "method oopsDo");
        check(hasMethod(c, "isInStack"), "method isInStack");
        check(hasMethod(c, "suspendFlags"), "method suspendFlags");
        check(hasMethod(c, "tlab"), "method tlab");
        check(hasMethod(c, "allocatedBytes"), "method allocatedBytes");
        check(hasMethod(c, "isVMThread"), "method isVMThread");
        check(hasMethod(c, "isHiddenFromExternalView"), "method isHiddenFromExternalView");
        check(hasMethod(c, "getCurrentPendingMonitor"), "method getCurrentPendingMonitor");
        check(hasMethod(c, "getCurrentWaitingMonitor"), "method getCurrentWaitingMonitor");
        check(hasMethod(c, "osThread"), "method osThread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
