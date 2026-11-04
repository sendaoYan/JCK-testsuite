/*
 * @test
 * @summary sun.jvm.hotspot.debugger.remote.RemoteDebugger conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.remote:+open
 * @run main RemoteDebuggerTest
 */
import java.lang.reflect.*;

public class RemoteDebuggerTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.remote.RemoteDebugger", false, RemoteDebuggerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.rmi.Remote"), "interface java.rmi.Remote");
        check(hasMethod(c, "getOS"), "method getOS");
        check(hasMethod(c, "getCPU"), "method getCPU");
        check(hasMethod(c, "getMachineDescription"), "method getMachineDescription");
        check(hasMethod(c, "hasConsole"), "method hasConsole");
        check(hasMethod(c, "consoleExecuteCommand"), "method consoleExecuteCommand");
        check(hasMethod(c, "getConsolePrompt"), "method getConsolePrompt");
        check(hasMethod(c, "getJBooleanSize"), "method getJBooleanSize");
        check(hasMethod(c, "getJByteSize"), "method getJByteSize");
        check(hasMethod(c, "getJCharSize"), "method getJCharSize");
        check(hasMethod(c, "getJDoubleSize"), "method getJDoubleSize");
        check(hasMethod(c, "getJFloatSize"), "method getJFloatSize");
        check(hasMethod(c, "getJIntSize"), "method getJIntSize");
        check(hasMethod(c, "getJLongSize"), "method getJLongSize");
        check(hasMethod(c, "getJShortSize"), "method getJShortSize");
        check(hasMethod(c, "getHeapOopSize"), "method getHeapOopSize");
        check(hasMethod(c, "getNarrowOopBase"), "method getNarrowOopBase");
        check(hasMethod(c, "getNarrowOopShift"), "method getNarrowOopShift");
        check(hasMethod(c, "getKlassPtrSize"), "method getKlassPtrSize");
        check(hasMethod(c, "getNarrowKlassBase"), "method getNarrowKlassBase");
        check(hasMethod(c, "getNarrowKlassShift"), "method getNarrowKlassShift");
        check(hasMethod(c, "readBytesFromProcess"), "method readBytesFromProcess");
        check(hasMethod(c, "lookupInProcess"), "method lookupInProcess");
        check(hasMethod(c, "getThreadIntegerRegisterSet"), "method getThreadIntegerRegisterSet");
        check(hasMethod(c, "areThreadsEqual"), "method areThreadsEqual");
        check(hasMethod(c, "getThreadHashCode"), "method getThreadHashCode");
        check(hasMethod(c, "execCommandOnServer"), "method execCommandOnServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
