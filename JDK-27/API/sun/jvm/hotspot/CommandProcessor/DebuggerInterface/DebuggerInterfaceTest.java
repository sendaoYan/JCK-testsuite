/*
 * @test
 * @summary sun.jvm.hotspot.CommandProcessor.DebuggerInterface conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot:+open
 * @run main DebuggerInterfaceTest
 */
import java.lang.reflect.*;

public class DebuggerInterfaceTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.CommandProcessor$DebuggerInterface", false, DebuggerInterfaceTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "attach"), "method attach");
        check(hasMethod(c, "detach"), "method detach");
        check(hasMethod(c, "getAgent"), "method getAgent");
        check(hasMethod(c, "isAttached"), "method isAttached");
        check(hasMethod(c, "reattach"), "method reattach");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
