/*
 * @test
 * @summary sun.jvm.hotspot.ui.table.TableModelComparator conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.ui.table:+open
 * @run main TableModelComparatorTest
 */
import java.lang.reflect.*;

public class TableModelComparatorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.ui.table.TableModelComparator", false, TableModelComparatorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.util.Comparator"), "interface java.util.Comparator");
        check(hasMethod(c, "compare"), "method compare");
        check(hasMethod(c, "addColumn"), "method addColumn");
        check(hasMethod(c, "getColumn"), "method getColumn");
        check(hasMethod(c, "setAscending"), "method setAscending");
        check(hasMethod(c, "isAscending"), "method isAscending");
        check(hasMethod(c, "getValueForColumn"), "method getValueForColumn");
        check(hasField(c, "model"), "field model");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
