/*
 * @test
 * @summary sun.jvm.hotspot.debugger.bsd.BsdDebuggerLocal conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.bsd:+open
 * @run main BsdDebuggerLocalTest
 */
import java.lang.reflect.*;

public class BsdDebuggerLocalTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.bsd.BsdDebuggerLocal", false, BsdDebuggerLocalTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.debugger.DebuggerBase".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.bsd.BsdDebugger"), "interface sun.jvm.hotspot.debugger.bsd.BsdDebugger");
        check(hasMethod(c, "lookup"), "method lookup");
        check(hasMethod(c, "attach"), "method attach");
        check(hasMethod(c, "detach"), "method detach");
        check(hasMethod(c, "lookupOop"), "method lookupOop");
        check(hasMethod(c, "newAddress"), "method newAddress");
        check(hasMethod(c, "hasProcessList"), "method hasProcessList");
        check(hasMethod(c, "getProcessList"), "method getProcessList");
        check(hasMethod(c, "parseAddress"), "method parseAddress");
        check(hasMethod(c, "getAddressValue"), "method getAddressValue");
        check(hasMethod(c, "getOS"), "method getOS");
        check(hasMethod(c, "getCPU"), "method getCPU");
        check(hasMethod(c, "getMachineDescription"), "method getMachineDescription");
        check(hasMethod(c, "hasConsole"), "method hasConsole");
        check(hasMethod(c, "consoleExecuteCommand"), "method consoleExecuteCommand");
        check(hasMethod(c, "getConsolePrompt"), "method getConsolePrompt");
        check(hasMethod(c, "getCDebugger"), "method getCDebugger");
        check(hasMethod(c, "readBytesFromProcess"), "method readBytesFromProcess");
        check(hasMethod(c, "getThreadForIdentifierAddress"), "method getThreadForIdentifierAddress");
        check(hasMethod(c, "getThreadForThreadId"), "method getThreadForThreadId");
        check(hasMethod(c, "getAddressSize"), "method getAddressSize");
        check(hasMethod(c, "getLoadObjectList"), "method getLoadObjectList");
        check(hasMethod(c, "readOopHandle"), "method readOopHandle");
        check(hasMethod(c, "getThreadList"), "method getThreadList");
        check(hasMethod(c, "addressValueToString"), "method addressValueToString");
        check(hasMethod(c, "readAddress"), "method readAddress");
        check(hasMethod(c, "readCompOopAddress"), "method readCompOopAddress");
        check(hasMethod(c, "readCompKlassAddress"), "method readCompKlassAddress");
        check(hasMethod(c, "readCompOopHandle"), "method readCompOopHandle");
        check(hasMethod(c, "getThreadIntegerRegisterSet"), "method getThreadIntegerRegisterSet");
        check(hasMethod(c, "getJavaThreadsInfo"), "method getJavaThreadsInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
