/*
 * @test
 * @summary java.awt.Container conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ContainerTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class ContainerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static class Box extends Component {
        final Dimension pref;
        Box(int w, int h) { pref = new Dimension(w, h); }
        public Dimension getPreferredSize() { return pref; }
        public Dimension getMinimumSize() { return pref; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Container");
        check(c != null, "type loads: java.awt.Container");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        for (String name : new String[]{"getComponentCount", "getComponent", "getComponents",
                "add", "remove", "removeAll", "setLayout", "getLayout", "getInsets",
                "doLayout", "validate", "invalidate", "isAncestorOf", "getComponentAt",
                "findComponentAt", "setFocusTraversalPolicy", "getFocusTraversalPolicy"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Container c = new Container();
        check(c.getComponentCount() == 0, "initial empty");

        Box b1 = new Box(10, 10);
        Box b2 = new Box(20, 20);
        Box b3 = new Box(30, 30);

        check(c.add(b1) == b1, "add returns component");
        c.add(b2);
        c.add(b3, 0); // insert at front
        check(c.getComponentCount() == 3, "count 3");
        check(c.getComponent(0) == b3, "inserted at index 0");
        check(c.getComponent(1) == b1, "component order");
        check(c.getComponent(2) == b2, "component order");
        check(c.getComponents().length == 3, "getComponents length");
        check(c.getComponents()[0] == b3, "getComponents order");

        check(c.isAncestorOf(b1), "isAncestorOf");
        check(!c.isAncestorOf(c), "not ancestor of self");

        c.remove(b1);
        check(c.getComponentCount() == 2, "remove(Component)");
        check(c.getComponent(0) == b3 && c.getComponent(1) == b2, "order after remove");
        c.remove(0);
        check(c.getComponentCount() == 1 && c.getComponent(0) == b2, "remove(int)");
        c.removeAll();
        check(c.getComponentCount() == 0, "removeAll");

        // layout with FlowLayout (displayable so validate() can set the valid flag)
        Frame frame = new Frame("ContainerTest");
        frame.setLayout(null);
        frame.setSize(200, 200);
        frame.setVisible(true);
        try {
            Container panel = new Container();
            FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 0, 0);
            panel.setLayout(fl);
            check(panel.getLayout() == fl, "setLayout/getLayout");
            Box a = new Box(20, 10);
            Box b = new Box(30, 10);
            panel.add(a);
            panel.add(b);
            panel.setSize(100, 50);
            frame.add(panel);
            panel.doLayout();
            check(a.getBounds().equals(new Rectangle(0, 0, 20, 10)), "laid out first");
            check(b.getBounds().equals(new Rectangle(20, 0, 30, 10)), "laid out second");
            panel.invalidate();
            check(!panel.isValid(), "invalidate clears valid");
            panel.validate();
            check(panel.isValid(), "validate sets valid");

            check(panel.getInsets() != null, "getInsets non-null");
            check(panel.findComponentAt(5, 5) != null, "findComponentAt non-null");
            check(panel.getComponentAt(5, 5) != null, "getComponentAt non-null");

            // FocusTraversalPolicy (only returned when the container is a focus cycle root)
            FocusTraversalPolicy ftp = new DefaultFocusTraversalPolicy();
            panel.setFocusCycleRoot(true);
            panel.setFocusTraversalPolicy(ftp);
            check(panel.getFocusTraversalPolicy() == ftp, "focus traversal policy");
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
