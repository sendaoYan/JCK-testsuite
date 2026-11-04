/*
 * @test
 * @summary sun.jvm.hotspot.tools.HeapSummary conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.tools:+open
 * @run main HeapSummaryTest
 */
import java.lang.reflect.*;

public class HeapSummaryTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.tools.HeapSummary", false, HeapSummaryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.tools.Tool".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "run"), "method run");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "printG1HeapSummary"), "method printG1HeapSummary");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
