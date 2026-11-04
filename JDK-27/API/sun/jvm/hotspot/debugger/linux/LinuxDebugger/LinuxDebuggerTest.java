/*
 * @test
 * @summary sun.jvm.hotspot.debugger.linux.LinuxDebugger conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.linux:+open
 * @run main LinuxDebuggerTest
 */
import java.lang.reflect.*;

public class LinuxDebuggerTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.linux.LinuxDebugger", false, LinuxDebuggerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.JVMDebugger"), "interface sun.jvm.hotspot.debugger.JVMDebugger");
        check(hasMethod(c, "lookup"), "method lookup");
        check(hasMethod(c, "getAddressValue"), "method getAddressValue");
        check(hasMethod(c, "getLoadObjectList"), "method getLoadObjectList");
        check(hasMethod(c, "readOopHandle"), "method readOopHandle");
        check(hasMethod(c, "getThreadList"), "method getThreadList");
        check(hasMethod(c, "demangle"), "method demangle");
        check(hasMethod(c, "readCInteger"), "method readCInteger");
        check(hasMethod(c, "readJBoolean"), "method readJBoolean");
        check(hasMethod(c, "readJByte"), "method readJByte");
        check(hasMethod(c, "readJChar"), "method readJChar");
        check(hasMethod(c, "readJDouble"), "method readJDouble");
        check(hasMethod(c, "readJFloat"), "method readJFloat");
        check(hasMethod(c, "readJInt"), "method readJInt");
        check(hasMethod(c, "readJLong"), "method readJLong");
        check(hasMethod(c, "readJShort"), "method readJShort");
        check(hasMethod(c, "addressValueToString"), "method addressValueToString");
        check(hasMethod(c, "readAddress"), "method readAddress");
        check(hasMethod(c, "readCompOopAddress"), "method readCompOopAddress");
        check(hasMethod(c, "readCompKlassAddress"), "method readCompKlassAddress");
        check(hasMethod(c, "readCompOopHandle"), "method readCompOopHandle");
        check(hasMethod(c, "getThreadIntegerRegisterSet"), "method getThreadIntegerRegisterSet");
        check(hasMethod(c, "findLibPtrByAddress"), "method findLibPtrByAddress");
        check(hasMethod(c, "isSignalTrampoline"), "method isSignalTrampoline");
        check(hasField(c, "SIGTRAMP_NAMES"), "field SIGTRAMP_NAMES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
