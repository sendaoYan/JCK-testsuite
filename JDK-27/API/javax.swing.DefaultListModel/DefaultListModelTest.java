/*
 * @test
 * @summary javax.swing.DefaultListModel conformance: white-box structure + black-box behavior.
 * @run main DefaultListModelTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.DefaultListModel;

public class DefaultListModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultListModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractListModel"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultListModel<String> m = new DefaultListModel<>();
        check(m.isEmpty(), "empty initially");

        m.addElement("a");
        m.addElement("b");
        m.addElement("c");
        check(m.getSize() == 3 && m.size() == 3, "size");
        check("b".equals(m.getElementAt(1)), "getElementAt");
        check("c".equals(m.elementAt(2)), "elementAt");
        check("a".equals(m.firstElement()), "firstElement");
        check("c".equals(m.lastElement()), "lastElement");

        // search
        check(m.indexOf("b") == 1, "indexOf");
        check(m.lastIndexOf("a") == 0, "lastIndexOf");
        check(m.contains("c"), "contains");

        // mutation
        m.insertElementAt("x", 0);
        check("x".equals(m.get(0)), "insertElementAt");
        m.setElementAt("y", 1);
        check("y".equals(m.getElementAt(1)), "setElementAt");
        m.removeElementAt(0);
        check(!"x".equals(m.getElementAt(0)), "removeElementAt");
        m.removeElement("b");
        check(!m.contains("b"), "removeElement");

        // enumeration + copy
        Enumeration<String> en = m.elements();
        int count = 0;
        while (en.hasMoreElements()) { en.nextElement(); count++; }
        check(count == m.size(), "elements enumeration");

        Object[] arr = m.toArray();
        check(arr.length == m.size(), "toArray");
        String[] dest = new String[m.size()];
        m.copyInto(dest);
        check(Arrays.equals(arr, dest), "copyInto");

        m.clear();
        check(m.isEmpty(), "clear");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
