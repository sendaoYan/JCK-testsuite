/*
 * @test
 * @summary javax.swing.DefaultRowSorter conformance: type shape via reflection (headless-safe).
 * @run main DefaultRowSorterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultRowSorterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultRowSorter", false, DefaultRowSorterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.RowSorter"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getModel"), "method getModel");
        check(names.contains("setSortable"), "method setSortable");
        check(names.contains("isSortable"), "method isSortable");
        check(names.contains("setSortKeys"), "method setSortKeys");
        check(names.contains("getSortKeys"), "method getSortKeys");
        check(names.contains("setMaxSortKeys"), "method setMaxSortKeys");
        check(names.contains("getMaxSortKeys"), "method getMaxSortKeys");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
