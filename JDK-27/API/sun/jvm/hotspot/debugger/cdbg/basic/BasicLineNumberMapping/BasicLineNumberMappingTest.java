/*
 * @test
 * @summary sun.jvm.hotspot.debugger.cdbg.basic.BasicLineNumberMapping conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.cdbg.basic:+open
 * @run main BasicLineNumberMappingTest
 */
import java.lang.reflect.*;

public class BasicLineNumberMappingTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.cdbg.basic.BasicLineNumberMapping", false, BasicLineNumberMappingTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "sort"), "method sort");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "lineNumberForPC"), "method lineNumberForPC");
        check(hasMethod(c, "recomputeEndPCs"), "method recomputeEndPCs");
        check(hasMethod(c, "addLineNumberInfo"), "method addLineNumberInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
