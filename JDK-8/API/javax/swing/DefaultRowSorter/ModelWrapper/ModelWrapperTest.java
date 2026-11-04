/*
 * @test
 * @summary javax.swing.DefaultRowSorter.ModelWrapper conformance: type shape via reflection (headless-safe).
 * @run main ModelWrapperTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ModelWrapperTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultRowSorter$ModelWrapper", false, ModelWrapperTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getModel"), "method getModel");
        check(names.contains("getColumnCount"), "method getColumnCount");
        check(names.contains("getRowCount"), "method getRowCount");
        check(names.contains("getValueAt"), "method getValueAt");
        check(names.contains("getStringValueAt"), "method getStringValueAt");
        check(names.contains("getIdentifier"), "method getIdentifier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
