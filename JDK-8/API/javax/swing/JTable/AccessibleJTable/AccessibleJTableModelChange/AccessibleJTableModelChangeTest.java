/*
 * @test
 * @summary javax.swing.JTable.AccessibleJTable.AccessibleJTableModelChange conformance: type shape via reflection (headless-safe).
 * @run main AccessibleJTableModelChangeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleJTableModelChangeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.JTable$AccessibleJTable$AccessibleJTableModelChange", false, AccessibleJTableModelChangeTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleTableModelChange"), "implements javax.accessibility.AccessibleTableModelChange");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getType"), "method getType");
        check(names.contains("getFirstRow"), "method getFirstRow");
        check(names.contains("getLastRow"), "method getLastRow");
        check(names.contains("getFirstColumn"), "method getFirstColumn");
        check(names.contains("getLastColumn"), "method getLastColumn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
