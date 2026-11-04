/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicLineNumberInfo conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicLineNumberInfoTest
 */
import java.lang.reflect.*;

public class BasicLineNumberInfoTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicLineNumberInfo", false, BasicLineNumberInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.debugger.cdbg.LineNumberInfo"), "interface sun.jvm.hotspot.debugger.cdbg.LineNumberInfo");
        check(hasMethod(c, "getLineNumber"), "method getLineNumber");
        check(hasMethod(c, "getSourceFileName"), "method getSourceFileName");
        check(hasMethod(c, "getStartPC"), "method getStartPC");
        check(hasMethod(c, "getEndPC"), "method getEndPC");
        check(hasMethod(c, "setEndPC"), "method setEndPC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
