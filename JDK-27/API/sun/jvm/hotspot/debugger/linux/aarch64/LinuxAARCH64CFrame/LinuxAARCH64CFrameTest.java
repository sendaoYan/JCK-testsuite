/*
 * @test
 * @summary sun.jvm.hotspot.debugger.linux.aarch64.LinuxAARCH64CFrame conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.linux.aarch64:+open
 * @run main LinuxAARCH64CFrameTest
 */
import java.lang.reflect.*;

public class LinuxAARCH64CFrameTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.linux.aarch64.LinuxAARCH64CFrame", false, LinuxAARCH64CFrameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.jvm.hotspot.debugger.linux.DwarfCFrame".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "sender"), "method sender");
        check(hasMethod(c, "getTopFrame"), "method getTopFrame");
        check(hasMethod(c, "toFrame"), "method toFrame");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
