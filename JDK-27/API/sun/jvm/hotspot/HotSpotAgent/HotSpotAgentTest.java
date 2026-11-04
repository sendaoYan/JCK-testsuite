/*
 * @test
 * @summary sun.jvm.hotspot.HotSpotAgent conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot:+open
 * @run main HotSpotAgentTest
 */
import java.lang.reflect.*;

public class HotSpotAgentTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.HotSpotAgent", false, HotSpotAgentTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "attach"), "method attach");
        check(hasMethod(c, "detach"), "method detach");
        check(hasMethod(c, "startServer"), "method startServer");
        check(hasMethod(c, "getDebugger"), "method getDebugger");
        check(hasMethod(c, "getTypeDataBase"), "method getTypeDataBase");
        check(hasMethod(c, "shutdownServer"), "method shutdownServer");
        check(hasMethod(c, "getStartupMode"), "method getStartupMode");
        check(hasField(c, "PROCESS_MODE"), "field PROCESS_MODE");
        check(hasField(c, "CORE_FILE_MODE"), "field CORE_FILE_MODE");
        check(hasField(c, "REMOTE_MODE"), "field REMOTE_MODE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
