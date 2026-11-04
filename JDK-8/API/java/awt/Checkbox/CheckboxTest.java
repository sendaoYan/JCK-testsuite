/*
 * @test
 * @key headful
 * @summary java.awt.Checkbox conformance: class reflection.
 * @run main CheckboxTest
 */

import java.awt.*;

public class CheckboxTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractCheckbox() throws Exception {
        // constructor matrix
        Checkbox d = new Checkbox();
        check("".equals(d.getLabel()) && !d.getState() && d.getCheckboxGroup() == null,
              "Checkbox() defaults: empty label, off, no group");
        check(new Checkbox(null).getLabel() == null, "Checkbox(null) retains null label");
        Checkbox t1 = new Checkbox("a", true);
        check(t1.getState() && t1.getCheckboxGroup() == null, "Checkbox(String,boolean)");
        Checkbox t2 = new Checkbox("a", true, (CheckboxGroup) null);
        check(t2.getState() && t2.getCheckboxGroup() == null, "Checkbox(String,boolean,null)");
        // label null roundtrip
        t1.setLabel(null);
        check(t1.getLabel() == null, "setLabel(null) retains null");

        // state cycle; programmatic setState never dispatches ItemEvent without a peer
        boolean[] ev = { false };
        Checkbox c1 = new Checkbox("c1", false, null);
        c1.addItemListener(e -> ev[0] = true);
        c1.setState(true);
        check(c1.getState(), "setState(true)");
        c1.setState(false);
        check(!c1.getState(), "setState(false)");
        java.awt.EventQueue.invokeAndWait(() -> { });
        check(!ev[0], "no ItemEvent from programmatic setState");

        // listener registry with duplicates (on a fresh component: the no-event
        // probe above deliberately left its own listener registered)
        java.awt.event.ItemListener il = e -> { };
        Checkbox reg = new Checkbox("reg");
        reg.addItemListener(il); reg.addItemListener(il);
        check(reg.getItemListeners().length == 2, "duplicate ItemListeners registered");
        reg.removeItemListener(il);
        check(reg.getItemListeners().length == 1, "removeItemListener removes one instance");
        reg.removeItemListener(il);
        check(reg.getItemListeners().length == 0, "registry empties");
        check(reg.getListeners(java.awt.event.ItemListener.class).length == 0, "getListeners agrees");

        // group membership: constructing with state=true steals the selection
        CheckboxGroup g = new CheckboxGroup();
        Checkbox a = new Checkbox("a", true, g);
        check(g.getSelectedCheckbox() == a, "first true checkbox becomes selection");
        Checkbox b = new Checkbox("b", true, g);
        check(g.getSelectedCheckbox() == b && !a.getState() && b.getState(),
              "second true checkbox steals selection");

        // setState(true) moves selection, old member becomes false
        a.setState(true);
        check(g.getSelectedCheckbox() == a && !b.getState(), "setState(true) moves group selection");
        // setState(false) on the group-selected checkbox is ignored entirely:
        // the checkbox keeps its state and the group keeps the reference (historic AWT behavior)
        a.setState(false);
        check(a.getState(), "setState(false) on the group selection is ignored");
        check(g.getSelectedCheckbox() == a, "group reference kept after member setState(false)");
        // clearing via the group API unchecks the member
        g.setSelectedCheckbox(null);
        check(g.getSelectedCheckbox() == null && !a.getState() && !b.getState(),
              "setSelectedCheckbox(null) unchecks members");
        // detaching keeps own state, clears both directions of the link
        Checkbox x = new Checkbox("x", true, g);
        check(g.getSelectedCheckbox() == x, "x selected");
        x.setCheckboxGroup(null);
        check(x.getCheckboxGroup() == null && x.getState(), "detach keeps own state");
        check(g.getSelectedCheckbox() == null, "detach clears group selection");
        // getSelectedObjects mirrors state
        check(x.getSelectedObjects() != null && x.getSelectedObjects().length == 1
              && "x".equals(x.getSelectedObjects()[0]), "getSelectedObjects when on");
        x.setState(false);
        check(x.getSelectedObjects() == null, "getSelectedObjects null when off");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Checkbox");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.ItemSelectable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getLabel"), "method getLabel");
        check(hasMethod(c, "setLabel"), "method setLabel");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "setState"), "method setState");
        check(hasMethod(c, "getSelectedObjects"), "method getSelectedObjects");
        check(hasMethod(c, "getCheckboxGroup"), "method getCheckboxGroup");
        check(hasMethod(c, "setCheckboxGroup"), "method setCheckboxGroup");
        check(hasMethod(c, "addItemListener"), "method addItemListener");
        check(hasMethod(c, "removeItemListener"), "method removeItemListener");
        check(hasMethod(c, "getItemListeners"), "method getItemListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractCheckbox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
