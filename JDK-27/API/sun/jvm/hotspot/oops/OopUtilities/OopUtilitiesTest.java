/*
 * @test
 * @summary sun.jvm.hotspot.oops.OopUtilities conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main OopUtilitiesTest
 */
import java.lang.reflect.*;

public class OopUtilitiesTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.OopUtilities", false, OopUtilitiesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "threadOopGetName"), "method threadOopGetName");
        check(hasMethod(c, "threadOopGetParkBlocker"), "method threadOopGetParkBlocker");
        check(hasMethod(c, "threadOopGetDaemon"), "method threadOopGetDaemon");
        check(hasMethod(c, "threadOopGetPriority"), "method threadOopGetPriority");
        check(hasMethod(c, "threadOopGetThreadStatusName"), "method threadOopGetThreadStatusName");
        check(hasMethod(c, "threadOopGetTID"), "method threadOopGetTID");
        check(hasMethod(c, "threadOopGetThreadStatus"), "method threadOopGetThreadStatus");
        check(hasMethod(c, "stringOopToString"), "method stringOopToString");
        check(hasMethod(c, "escapeString"), "method escapeString");
        check(hasMethod(c, "byteArrayToString"), "method byteArrayToString");
        check(hasMethod(c, "threadOopGetJavaThread"), "method threadOopGetJavaThread");
        check(hasMethod(c, "charArrayToString"), "method charArrayToString");
        check(hasMethod(c, "stringOopToEscapedString"), "method stringOopToEscapedString");
        check(hasMethod(c, "abstractOwnableSynchronizerGetOwnerThread"), "method abstractOwnableSynchronizerGetOwnerThread");
        check(hasField(c, "THREAD_STATUS_NEW"), "field THREAD_STATUS_NEW");
        check(hasField(c, "THREAD_STATUS_RUNNABLE"), "field THREAD_STATUS_RUNNABLE");
        check(hasField(c, "THREAD_STATUS_SLEEPING"), "field THREAD_STATUS_SLEEPING");
        check(hasField(c, "THREAD_STATUS_IN_OBJECT_WAIT"), "field THREAD_STATUS_IN_OBJECT_WAIT");
        check(hasField(c, "THREAD_STATUS_IN_OBJECT_WAIT_TIMED"), "field THREAD_STATUS_IN_OBJECT_WAIT_TIMED");
        check(hasField(c, "THREAD_STATUS_PARKED"), "field THREAD_STATUS_PARKED");
        check(hasField(c, "THREAD_STATUS_PARKED_TIMED"), "field THREAD_STATUS_PARKED_TIMED");
        check(hasField(c, "THREAD_STATUS_BLOCKED_ON_MONITOR_ENTER"), "field THREAD_STATUS_BLOCKED_ON_MONITOR_ENTER");
        check(hasField(c, "THREAD_STATUS_TERMINATED"), "field THREAD_STATUS_TERMINATED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
