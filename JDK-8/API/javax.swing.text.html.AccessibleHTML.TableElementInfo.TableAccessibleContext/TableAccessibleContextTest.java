/*
 * @test
 * @summary javax.swing.text.html.AccessibleHTML.TableElementInfo.TableAccessibleContext conformance: type shape via reflection (headless-safe).
 * @run main TableAccessibleContextTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TableAccessibleContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.AccessibleHTML$TableElementInfo$TableAccessibleContext", false, TableAccessibleContextTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.html.AccessibleHTML$HTMLAccessibleContext"), "superclass");
        check(hasInterface(c, "javax.accessibility.AccessibleTable"), "implements javax.accessibility.AccessibleTable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAccessibleName"), "method getAccessibleName");
        check(names.contains("getAccessibleDescription"), "method getAccessibleDescription");
        check(names.contains("getAccessibleRole"), "method getAccessibleRole");
        check(names.contains("getAccessibleIndexInParent"), "method getAccessibleIndexInParent");
        check(names.contains("getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(names.contains("getAccessibleChild"), "method getAccessibleChild");
        check(names.contains("getAccessibleTable"), "method getAccessibleTable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
