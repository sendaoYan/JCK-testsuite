/*
 * @test
 * @summary javax.swing.table.JTableHeader.AccessibleJTableHeader.AccessibleJTableHeaderEntry conformance: type shape via reflection (headless-safe).
 * @run main AccessibleJTableHeaderEntryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AccessibleJTableHeaderEntryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.JTableHeader$AccessibleJTableHeader$AccessibleJTableHeaderEntry", false, AccessibleJTableHeaderEntryTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(hasInterface(c, "javax.accessibility.AccessibleComponent"), "implements javax.accessibility.AccessibleComponent");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleContext"), "method getAccessibleContext");
        check(names.contains("getAccessibleName"), "method getAccessibleName");
        check(names.contains("setAccessibleName"), "method setAccessibleName");
        check(names.contains("getAccessibleDescription"), "method getAccessibleDescription");
        check(names.contains("setAccessibleDescription"), "method setAccessibleDescription");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleStateSet"), "method getAccessibleStateSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
