/*
 * @test
 * @summary javax.swing.table.TableColumnModel conformance: type shape via reflection (headless-safe).
 * @run main TableColumnModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TableColumnModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableColumnModel", false, TableColumnModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addColumn"), "method addColumn");
        check(names.contains("removeColumn"), "method removeColumn");
        check(names.contains("moveColumn"), "method moveColumn");
        check(names.contains("setColumnMargin"), "method setColumnMargin");
        check(names.contains("getColumnCount"), "method getColumnCount");
        check(names.contains("getColumns"), "method getColumns");
        check(names.contains("getColumnIndex"), "method getColumnIndex");
        check(names.contains("getColumn"), "method getColumn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
