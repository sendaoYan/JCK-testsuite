/*
 * @test
 * @summary java.awt.Container conformance: structural reflection + black-box behavior.
 * @key headful
 * @run main ContainerTest
 */

import java.awt.*;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.List;

public class ContainerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError("expected " + ex.getSimpleName() + ": " + msg);
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), msg + " -> " + t.getClass().getSimpleName());
        }
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static class Box extends Component {
        final Dimension pref;
        Box(int w, int h) { pref = new Dimension(w, h); }
        public Dimension getPreferredSize() { return pref; }
        public Dimension getMinimumSize() { return pref; }
    }

    // ---- Part 1: structural reflection (public API surface) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.awt.Container");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getComponentCount"), "method getComponentCount");
        check(hasMethod(c, "countComponents"), "method countComponents");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "getComponents"), "method getComponents");
        check(hasMethod(c, "getInsets"), "method getInsets");
        check(hasMethod(c, "insets"), "method insets");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "setComponentZOrder"), "method setComponentZOrder");
        check(hasMethod(c, "getComponentZOrder"), "method getComponentZOrder");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "getLayout"), "method getLayout");
        check(hasMethod(c, "setLayout"), "method setLayout");
        check(hasMethod(c, "doLayout"), "method doLayout");
        check(hasMethod(c, "layout"), "method layout");
        check(hasMethod(c, "isValidateRoot"), "method isValidateRoot");
        check(hasMethod(c, "invalidate"), "method invalidate");
        check(hasMethod(c, "validate"), "method validate");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "getMaximumSize"), "method getMaximumSize");
        check(hasMethod(c, "getAlignmentX"), "method getAlignmentX");
        check(hasMethod(c, "getAlignmentY"), "method getAlignmentY");
        check(hasMethod(c, "paint"), "method paint");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "paintComponents"), "method paintComponents");
        check(hasMethod(c, "printComponents"), "method printComponents");
        check(hasMethod(c, "addContainerListener"), "method addContainerListener");
        check(hasMethod(c, "removeContainerListener"), "method removeContainerListener");
        check(hasMethod(c, "getContainerListeners"), "method getContainerListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "deliverEvent"), "method deliverEvent");
        check(hasMethod(c, "getComponentAt"), "method getComponentAt");
        check(hasMethod(c, "locate"), "method locate");
        check(hasMethod(c, "getMousePosition"), "method getMousePosition");
        check(hasMethod(c, "findComponentAt"), "method findComponentAt");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "isAncestorOf"), "method isAncestorOf");
        check(hasMethod(c, "list"), "method list");
        check(hasMethod(c, "setFocusTraversalKeys"), "method setFocusTraversalKeys");
        check(hasMethod(c, "getFocusTraversalKeys"), "method getFocusTraversalKeys");
        check(hasMethod(c, "areFocusTraversalKeysSet"), "method areFocusTraversalKeysSet");
        check(hasMethod(c, "isFocusCycleRoot"), "method isFocusCycleRoot");
        check(hasMethod(c, "setFocusTraversalPolicy"), "method setFocusTraversalPolicy");
        check(hasMethod(c, "getFocusTraversalPolicy"), "method getFocusTraversalPolicy");
        check(hasMethod(c, "isFocusTraversalPolicySet"), "method isFocusTraversalPolicySet");
        check(hasMethod(c, "setFocusCycleRoot"), "method setFocusCycleRoot");
        check(hasMethod(c, "setFocusTraversalPolicyProvider"), "method setFocusTraversalPolicyProvider");
        check(hasMethod(c, "isFocusTraversalPolicyProvider"), "method isFocusTraversalPolicyProvider");
        check(hasMethod(c, "transferFocusDownCycle"), "method transferFocusDownCycle");
        check(hasMethod(c, "applyComponentOrientation"), "method applyComponentOrientation");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Container c = new Container();
        check(c.getComponentCount() == 0, "initial empty");
        check(c.countComponents() == 0, "countComponents deprecated alias");

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
            panel.layout(); // deprecated alias
            check(b.getBounds().equals(new Rectangle(20, 0, 30, 10)), "layout() deprecated alias");
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

    // ---- Part 3: add overload family + index semantics + exceptions ----
    static void testAddOverloadsAndExceptions() {
        Container c = new Container();
        Box a = new Box(1, 1), b = new Box(2, 2), d = new Box(3, 3);

        // add(String, Component) returns the component
        check(c.add("North", a) == a, "add(String,comp) returns comp");
        check(c.getComponentCount() == 1 && c.getComponent(0) == a, "string constraints appends");
        // add(Component, int) returns the component
        check(c.add(b, 0) == b, "add(comp,int) returns comp");
        check(c.getComponent(0) == b && c.getComponent(1) == a, "insert at 0");
        // append at index == count
        c.add(d, 2);
        check(c.getComponent(2) == d, "add(comp,count) appends");
        // index -1 appends
        Box e = new Box(4, 4);
        c.add(e, -1);
        check(c.getComponent(3) == e, "add(comp,-1) appends");
        // void add(Component, Object)
        Box f = new Box(5, 5);
        c.add(f, BorderLayout.SOUTH);
        check(c.getComponentCount() == 5, "add(comp,constraints) adds");
        // void add(Component, Object, int)
        Box g = new Box(6, 6);
        c.add(g, BorderLayout.NORTH, 0);
        check(c.getComponent(0) == g, "add(comp,constraints,index) inserts");
        c.removeAll();

        // null component -> NullPointerException in every add form
        checkThrows(NullPointerException.class, () -> c.add((Component) null), "add(null)");
        checkThrows(NullPointerException.class, () -> c.add(null, 0), "add(null,int)");
        checkThrows(NullPointerException.class, () -> c.add("name", null), "add(String,null)");
        check(c.getComponentCount() == 0, "no partial state after null adds");

        // invalid indices -> IllegalArgumentException for add
        Box h = new Box(7, 7);
        checkThrows(IllegalArgumentException.class, () -> c.add(h, 99), "add(comp,99)");
        checkThrows(IllegalArgumentException.class, () -> c.add(h, -2), "add(comp,-2)");
        check(c.getComponentCount() == 0, "no partial state after bad index");

        // invalid indices for remove/getComponent -> ArrayIndexOutOfBoundsException
        c.add(h);
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.remove(5), "remove(5)");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.remove(-1), "remove(-1)");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.getComponent(5), "getComponent(5)");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> c.getComponent(-1), "getComponent(-1)");
        check(c.getComponentCount() == 1, "failed remove leaves state intact");

        // removing a non-child is a no-op
        Box stranger = new Box(8, 8);
        c.remove(stranger);
        check(c.getComponentCount() == 1, "remove(non-child) no-op");
        c.removeAll();
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> new Container().remove(0),
                    "remove(0) on empty container");
    }

    // ---- Part 4: getComponents copy semantics + re-parenting ----
    static void testComponentsCopyAndReparenting() {
        Container c1 = new Container();
        Container c2 = new Container();
        Box shared = new Box(2, 2);
        c1.add(shared);
        c1.add(new Box(1, 1));

        // returned array is a copy: mutating it does not affect the container
        Component[] arr = c1.getComponents();
        arr[0] = null;
        check(c1.getComponents()[0] == shared, "getComponents returns a copy");
        // a fresh array each call
        check(c1.getComponents() != c1.getComponents(), "getComponents fresh array");

        // re-adding to another container re-parents (removes from the old one)
        check(c2.add(shared) == shared, "re-add returns comp");
        check(c1.getComponentCount() == 1, "old parent lost the component");
        check(c2.getComponentCount() == 1, "new parent gained the component");
        check(shared.getParent() == c2, "parent pointer updated");

        // isAncestorOf is transitive
        Container outer = new Container();
        outer.add(c1);
        Box leaf = new Box(1, 1);
        c1.add(leaf);
        check(outer.isAncestorOf(leaf), "isAncestorOf transitive");
        check(!c1.isAncestorOf(outer), "isAncestorOf not inverted");
        check(!outer.isAncestorOf(outer), "isAncestorOf not reflexive");

        // z-order management
        Container z = new Container();
        Box x1 = new Box(1, 1), x2 = new Box(1, 1), x3 = new Box(1, 1);
        z.add(x1); z.add(x2); z.add(x3);
        check(z.getComponentZOrder(x1) == 0, "z order of first");
        check(z.getComponentZOrder(x3) == 2, "z order of last");
        z.setComponentZOrder(x1, 2);
        check(z.getComponentZOrder(x1) == 2, "z order after move");
        check(z.getComponent(0) == x2 && z.getComponent(2) == x1, "order after z move");
    }

    // ---- Part 5: container events + listener contracts ----
    static void testContainerEvents() {
        Container c = new Container();
        check(c.getContainerListeners().length == 0, "no container listeners initially");
        // null registration is a no-op
        c.addContainerListener(null);
        c.removeContainerListener(null);
        check(c.getContainerListeners().length == 0, "null container listener no-op");

        final List<String> log = new ArrayList<>();
        final Container source = c;
        ContainerListener cl = new ContainerAdapter() {
            public void componentAdded(ContainerEvent e) {
                log.add("ADD:" + e.getID() + ":" + (e.getSource() == source)
                        + ":" + (e.getChild() instanceof Box) + ":" + (e.getContainer() == source));
            }
            public void componentRemoved(ContainerEvent e) {
                log.add("REM:" + e.getID() + ":" + (e.getChild() instanceof Box));
            }
        };
        c.addContainerListener(cl);
        check(c.getContainerListeners().length == 1, "listener registered");

        Box b = new Box(3, 3);
        c.add(b); // synchronous delivery
        check(log.size() == 1, "componentAdded delivered synchronously");
        check(log.get(0).equals("ADD:" + ContainerEvent.COMPONENT_ADDED + ":true:true:true"),
              "added event id/source/child/container exact");
        c.remove(b);
        check(log.size() == 2, "componentRemoved delivered");
        check(log.get(1).equals("REM:" + ContainerEvent.COMPONENT_REMOVED + ":true"),
              "removed event id/child exact");
        check(ContainerEvent.COMPONENT_ADDED == 300, "COMPONENT_ADDED id 300");
        check(ContainerEvent.COMPONENT_REMOVED == 301, "COMPONENT_REMOVED id 301");

        // removal stops delivery
        c.removeContainerListener(cl);
        check(c.getContainerListeners().length == 0, "listener removed");
        Box b2 = new Box(1, 1);
        c.add(b2);
        c.remove(b2);
        check(log.size() == 2, "no events after listener removal");
    }

    // ---- Part 6: font/orientation propagation + nested hit tests ----
    static void testPropagationAndHitTests() {
        Container parent = new Container();
        Component child = new Box(1, 1);
        parent.add(child);
        Font f = new Font("Dialog", Font.PLAIN, 12);
        parent.setFont(f);
        check(f.equals(child.getFont()), "Container.setFont propagates to children");
        check(parent.getFont().equals(child.getFont()), "parent and child font equal");
        // clearing the parent font clears the child's inherited value too
        parent.setFont(null);
        check(child.getFont() == null, "setFont(null) clears on children");

        parent.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        check(child.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT,
              "applyComponentOrientation recurses");
        checkThrows(NullPointerException.class,
                    () -> parent.applyComponentOrientation(null),
                    "applyComponentOrientation(null)");

        // nested hit tests: getComponentAt returns the immediate child,
        // findComponentAt descends to the deepest visible child
        Container outer = new Container();
        Container inner = new Container();
        Box leaf = new Box(50, 50);
        outer.setBounds(0, 0, 200, 200);
        inner.setBounds(20, 20, 100, 100);
        leaf.setBounds(10, 10, 50, 50);
        outer.add(inner);
        inner.add(leaf);
        check(outer.getComponentAt(30, 30) == inner, "getComponentAt returns immediate child");
        check(outer.getComponentAt(5, 5) == outer, "getComponentAt on gap returns container");
        check(outer.findComponentAt(30, 30) == leaf, "findComponentAt descends to leaf");
        check(outer.findComponentAt(5, 5) == outer, "findComponentAt gap returns container");
        check(outer.getComponentAt(500, 500) == null, "getComponentAt miss null");
        check(outer.findComponentAt(500, 500) == null, "findComponentAt miss null");
        check(outer.getComponentAt(new Point(30, 30)) == inner, "getComponentAt(Point)");
        check(outer.locate(30, 30) == inner, "locate deprecated alias");

        // insets of a plain (non-window) container are all zero
        check(new Container().getInsets().equals(new Insets(0, 0, 0, 0)),
              "plain container insets zero");
        check(new Container().insets().equals(new Insets(0, 0, 0, 0)),
              "insets() deprecated alias zero");

        // layout management
        Container lc = new Container();
        check(lc.getLayout() == null, "plain container default layout null");
        lc.setLayout(new BorderLayout());
        check(lc.getLayout() instanceof BorderLayout, "BorderLayout installed");
        lc.setLayout(null);
        check(lc.getLayout() == null, "setLayout(null) clears");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        testAddOverloadsAndExceptions();
        testComponentsCopyAndReparenting();
        testContainerEvents();
        testPropagationAndHitTests();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
