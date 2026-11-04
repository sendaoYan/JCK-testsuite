/*
 * @test
 * @summary javax.swing.text.html.TableView.RowView conformance: type shape via reflection (headless-safe).
 * @run main RowViewTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RowViewTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.text.html.TableView$RowView", false, RowViewTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttributes"), "method getAttributes");
        check(names.contains("preferenceChanged"), "method preferenceChanged");
        check(names.contains("getMinimumSpan"), "method getMinimumSpan");
        check(names.contains("getMaximumSpan"), "method getMaximumSpan");
        check(names.contains("getPreferredSpan"), "method getPreferredSpan");
        check(names.contains("changedUpdate"), "method changedUpdate");
        check(names.contains("paint"), "method paint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
