/*
 * @test
 * @summary sun.jvm.hotspot.ui.HighPrecisionJScrollBar conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui:+open
 * @run main HighPrecisionJScrollBarTest
 */
import java.lang.reflect.*;

public class HighPrecisionJScrollBarTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.HighPrecisionJScrollBar", false, HighPrecisionJScrollBarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("javax.swing.JScrollBar".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "main"), "method main");
        check(hasMethod(c, "removeChangeListener"), "method removeChangeListener");
        check(hasMethod(c, "addChangeListener"), "method addChangeListener");
        check(hasMethod(c, "getValueHP"), "method getValueHP");
        check(hasMethod(c, "getMaximumHP"), "method getMaximumHP");
        check(hasMethod(c, "getMinimumHP"), "method getMinimumHP");
        check(hasMethod(c, "setValueHP"), "method setValueHP");
        check(hasMethod(c, "setVisibleAmountHP"), "method setVisibleAmountHP");
        check(hasMethod(c, "setBlockIncrementHP"), "method setBlockIncrementHP");
        check(hasMethod(c, "setUnitIncrementHP"), "method setUnitIncrementHP");
        check(hasMethod(c, "getVisibleAmountHP"), "method getVisibleAmountHP");
        check(hasMethod(c, "getUnitIncrementHP"), "method getUnitIncrementHP");
        check(hasMethod(c, "getBlockIncrementHP"), "method getBlockIncrementHP");
        check(hasMethod(c, "setMinimumHP"), "method setMinimumHP");
        check(hasMethod(c, "setMaximumHP"), "method setMaximumHP");
        check(hasMethod(c, "scrollUpOrLeft"), "method scrollUpOrLeft");
        check(hasMethod(c, "scrollDownOrRight"), "method scrollDownOrRight");
        check(hasMethod(c, "pageUpOrLeft"), "method pageUpOrLeft");
        check(hasMethod(c, "pageDownOrRight"), "method pageDownOrRight");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
