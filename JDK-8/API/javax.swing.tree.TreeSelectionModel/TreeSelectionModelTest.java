/*
 * @test
 * @summary javax.swing.tree.TreeSelectionModel conformance: type shape via reflection (headless-safe).
 * @run main TreeSelectionModelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TreeSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeSelectionModel", false, TreeSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSelectionMode"), "method setSelectionMode");
        check(names.contains("getSelectionMode"), "method getSelectionMode");
        check(names.contains("setSelectionPath"), "method setSelectionPath");
        check(names.contains("setSelectionPaths"), "method setSelectionPaths");
        check(names.contains("addSelectionPath"), "method addSelectionPath");
        check(names.contains("addSelectionPaths"), "method addSelectionPaths");
        check(names.contains("removeSelectionPath"), "method removeSelectionPath");
        check(names.contains("removeSelectionPaths"), "method removeSelectionPaths");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
