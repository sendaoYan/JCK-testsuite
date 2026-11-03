/*
 * @test
 * @summary javax.swing.ButtonGroup conformance: white-box structure + black-box behavior.
 * @run main ButtonGroupTest
 */

import java.lang.reflect.Modifier;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JRadioButton;

public class ButtonGroupTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ButtonGroup");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        ButtonGroup g = new ButtonGroup();
        check(g.getButtonCount() == 0, "initial count 0");
        check(g.getSelection() == null, "initial selection null");
        check(g.getElements().hasMoreElements() == false, "initial elements empty");

        JRadioButton b1 = new JRadioButton("one");
        JRadioButton b2 = new JRadioButton("two");
        g.add(b1);
        check(g.getButtonCount() == 1, "count 1 after add");
        g.add(b2);
        check(g.getButtonCount() == 2, "count 2 after add");

        Enumeration<AbstractButton> en = g.getElements();
        int n = 0;
        while (en.hasMoreElements()) { en.nextElement(); n++; }
        check(n == 2, "elements enumeration size 2");

        ButtonModel m1 = b1.getModel();
        ButtonModel m2 = b2.getModel();
        check(g.getSelection() == null, "no selection before setSelected");

        g.setSelected(m1, true);
        check(g.getSelection() == m1, "selection m1");
        check(g.isSelected(m1) == true, "isSelected m1");
        check(g.isSelected(m2) == false, "isSelected m2 false");
        check(b1.isSelected() == true, "b1 selected");
        check(b2.isSelected() == false, "b2 not selected");

        g.setSelected(m2, true);
        check(g.getSelection() == m2, "selection m2");
        check(b1.isSelected() == false, "b1 deselected");
        check(b2.isSelected() == true, "b2 selected");
        check(g.isSelected(m1) == false, "isSelected m1 false after switch");
        check(g.isSelected(m2) == true, "isSelected m2 true");

        g.setSelected(m1, false);
        check(g.getSelection() == m2, "setSelected false is no-op");

        g.clearSelection();
        check(g.getSelection() == null, "clearSelection null");
        check(b1.isSelected() == false, "b1 cleared");
        check(b2.isSelected() == false, "b2 cleared");

        g.remove(b1);
        check(g.getButtonCount() == 1, "count 1 after remove");
        g.remove(b1);
        check(g.getButtonCount() == 1, "remove twice still 1");
        g.add((AbstractButton) null);
        check(g.getButtonCount() == 1, "add null no-op");
        g.remove((AbstractButton) null);
        check(g.getButtonCount() == 1, "remove null no-op");

        ButtonGroup g2 = new ButtonGroup();
        JRadioButton p1 = new JRadioButton("p1");
        JRadioButton p2 = new JRadioButton("p2");
        p1.setSelected(true);
        p2.setSelected(true);
        g2.add(p1);
        check(g2.getSelection() == p1.getModel(), "preselected p1");
        g2.add(p2);
        check(g2.getSelection() == p1.getModel(), "p2 preselected does not override");
        check(p2.isSelected() == false, "p2 deselected on add");
        g2.remove(p1);
        check(g2.getSelection() == null, "removing selected clears selection");
    }
}
