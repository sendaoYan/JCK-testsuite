/*
 * @test
 * @summary javax.swing.ListSelectionModel conformance: type shape via reflection (headless-safe).
 * @run main ListSelectionModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ListSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.ListSelectionModel", false, ListSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSelectionInterval"), "method setSelectionInterval");
        check(names.contains("addSelectionInterval"), "method addSelectionInterval");
        check(names.contains("removeSelectionInterval"), "method removeSelectionInterval");
        check(names.contains("getMinSelectionIndex"), "method getMinSelectionIndex");
        check(names.contains("getMaxSelectionIndex"), "method getMaxSelectionIndex");
        check(names.contains("isSelectedIndex"), "method isSelectedIndex");
        check(names.contains("getAnchorSelectionIndex"), "method getAnchorSelectionIndex");
        check(names.contains("setAnchorSelectionIndex"), "method setAnchorSelectionIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
