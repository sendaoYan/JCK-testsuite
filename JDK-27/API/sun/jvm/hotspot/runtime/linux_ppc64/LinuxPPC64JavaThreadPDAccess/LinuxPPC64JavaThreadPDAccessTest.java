/*
 * @test
 * @summary sun.jvm.hotspot.runtime.linux_ppc64.LinuxPPC64JavaThreadPDAccess conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime.linux_ppc64:+open
 * @run main LinuxPPC64JavaThreadPDAccessTest
 */
import java.lang.reflect.*;

public class LinuxPPC64JavaThreadPDAccessTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.linux_ppc64.LinuxPPC64JavaThreadPDAccess", false, LinuxPPC64JavaThreadPDAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.runtime.JavaThreadPDAccess"), "interface sun.jvm.hotspot.runtime.JavaThreadPDAccess");
        check(hasMethod(c, "getCurrentFrameGuess"), "method getCurrentFrameGuess");
        check(hasMethod(c, "newRegisterMap"), "method newRegisterMap");
        check(hasMethod(c, "printInfoOn"), "method printInfoOn");
        check(hasMethod(c, "getLastJavaFP"), "method getLastJavaFP");
        check(hasMethod(c, "getBaseOfStackPointer"), "method getBaseOfStackPointer");
        check(hasMethod(c, "getLastFramePD"), "method getLastFramePD");
        check(hasMethod(c, "printThreadIDOn"), "method printThreadIDOn");
        check(hasMethod(c, "getThreadProxy"), "method getThreadProxy");
        check(hasMethod(c, "getLastJavaPC"), "method getLastJavaPC");
        check(hasMethod(c, "getLastSP"), "method getLastSP");
        check(hasMethod(c, "getLastFP"), "method getLastFP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
