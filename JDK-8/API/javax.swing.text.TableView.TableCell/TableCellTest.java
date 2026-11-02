/*
 * @test
 * @summary javax.swing.text.TableView.TableCell conformance: type shape via reflection (headless-safe).
 * @run main TableCellTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TableCellTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.TableView$TableCell", false, TableCellTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(hasInterface(c, "javax.swing.text.TableView$GridCell"), "implements javax.swing.text.TableView$GridCell");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getColumnCount"), "method getColumnCount");
        check(names.contains("getRowCount"), "method getRowCount");
        check(names.contains("setGridLocation"), "method setGridLocation");
        check(names.contains("getGridRow"), "method getGridRow");
        check(names.contains("getGridColumn"), "method getGridColumn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
