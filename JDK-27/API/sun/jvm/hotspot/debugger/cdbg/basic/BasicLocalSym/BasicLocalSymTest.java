/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicLocalSym conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicLocalSymTest
 */
import java.lang.reflect.*;

public class BasicLocalSymTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicLocalSym", false, BasicLocalSymTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.debugger.cdbg.basic.BasicSym".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.LocalSym"), "interface sun.jvm.hotspot.debugger.cdbg.LocalSym");
        check(hasMethod(c, "resolve"), "method resolve");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getFrameOffset"), "method getFrameOffset");
        check(hasMethod(c, "asLocal"), "method asLocal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
