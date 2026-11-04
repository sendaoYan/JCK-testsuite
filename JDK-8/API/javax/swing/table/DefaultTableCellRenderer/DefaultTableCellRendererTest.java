/*
 * @test
 * @summary javax.swing.table.DefaultTableCellRenderer conformance: type shape via reflection (headless-safe).
 * @run main DefaultTableCellRendererTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultTableCellRendererTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.table.DefaultTableCellRenderer", false, DefaultTableCellRendererTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JLabel"), "superclass");
        check(hasInterface(c, "javax.swing.table.TableCellRenderer"), "implements javax.swing.table.TableCellRenderer");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setForeground"), "method setForeground");
        check(names.contains("setBackground"), "method setBackground");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getTableCellRendererComponent"), "method getTableCellRendererComponent");
        check(names.contains("isOpaque"), "method isOpaque");
        check(names.contains("invalidate"), "method invalidate");
        check(names.contains("validate"), "method validate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
