/*
 * @test
 * @summary java.awt.AWTEventMulticaster conformance: class reflection.
 * @run main AWTEventMulticasterTest
 */

public class AWTEventMulticasterTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.AWTEventMulticaster");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.ActionListener", "java.awt.event.AdjustmentListener", "java.awt.event.ComponentListener", "java.awt.event.ContainerListener", "java.awt.event.FocusListener", "java.awt.event.HierarchyBoundsListener", "java.awt.event.HierarchyListener", "java.awt.event.InputMethodListener", "java.awt.event.ItemListener", "java.awt.event.KeyListener", "java.awt.event.MouseListener", "java.awt.event.MouseMotionListener", "java.awt.event.MouseWheelListener", "java.awt.event.TextListener", "java.awt.event.WindowFocusListener", "java.awt.event.WindowListener", "java.awt.event.WindowStateListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "componentResized"), "method componentResized");
        check(hasMethod(c, "componentMoved"), "method componentMoved");
        check(hasMethod(c, "componentShown"), "method componentShown");
        check(hasMethod(c, "componentHidden"), "method componentHidden");
        check(hasMethod(c, "componentAdded"), "method componentAdded");
        check(hasMethod(c, "componentRemoved"), "method componentRemoved");
        check(hasMethod(c, "focusGained"), "method focusGained");
        check(hasMethod(c, "focusLost"), "method focusLost");
        check(hasMethod(c, "keyTyped"), "method keyTyped");
        check(hasMethod(c, "keyPressed"), "method keyPressed");
        check(hasMethod(c, "keyReleased"), "method keyReleased");
        check(hasMethod(c, "mouseClicked"), "method mouseClicked");
        check(hasMethod(c, "mousePressed"), "method mousePressed");
        check(hasMethod(c, "mouseReleased"), "method mouseReleased");
        check(hasMethod(c, "mouseEntered"), "method mouseEntered");
        check(hasMethod(c, "mouseExited"), "method mouseExited");
        check(hasMethod(c, "mouseDragged"), "method mouseDragged");
        check(hasMethod(c, "mouseMoved"), "method mouseMoved");
        check(hasMethod(c, "windowOpened"), "method windowOpened");
        check(hasMethod(c, "windowClosing"), "method windowClosing");
        check(hasMethod(c, "windowClosed"), "method windowClosed");
        check(hasMethod(c, "windowIconified"), "method windowIconified");
        check(hasMethod(c, "windowDeiconified"), "method windowDeiconified");
        check(hasMethod(c, "windowActivated"), "method windowActivated");
        check(hasMethod(c, "windowDeactivated"), "method windowDeactivated");
        check(hasMethod(c, "windowStateChanged"), "method windowStateChanged");
        check(hasMethod(c, "windowGainedFocus"), "method windowGainedFocus");
        check(hasMethod(c, "windowLostFocus"), "method windowLostFocus");
        check(hasMethod(c, "actionPerformed"), "method actionPerformed");
        check(hasMethod(c, "itemStateChanged"), "method itemStateChanged");
        check(hasMethod(c, "adjustmentValueChanged"), "method adjustmentValueChanged");
        check(hasMethod(c, "textValueChanged"), "method textValueChanged");
        check(hasMethod(c, "inputMethodTextChanged"), "method inputMethodTextChanged");
        check(hasMethod(c, "caretPositionChanged"), "method caretPositionChanged");
        check(hasMethod(c, "hierarchyChanged"), "method hierarchyChanged");
        check(hasMethod(c, "ancestorMoved"), "method ancestorMoved");
        check(hasMethod(c, "ancestorResized"), "method ancestorResized");
        check(hasMethod(c, "mouseWheelMoved"), "method mouseWheelMoved");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "getListeners"), "method getListeners");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening ----
    static class Counting implements java.awt.event.ActionListener {
        int count; String tag;
        Counting(String t) { tag = t; }
        public void actionPerformed(java.awt.event.ActionEvent e) { count++; }
    }
    static void testContract() {
        // add dispatches to both listeners in order
        Counting a = new Counting("a"), b = new Counting("b");
        java.awt.event.ActionListener chain = java.awt.AWTEventMulticaster.add(a, b);
        check(chain != null, "add returns multicaster");
        java.awt.event.ActionEvent evt = new java.awt.event.ActionEvent("src",
                java.awt.event.ActionEvent.ACTION_PERFORMED, "cmd");
        chain.actionPerformed(evt);
        check(a.count == 1 && b.count == 1, "multicast dispatches to both");
        chain.actionPerformed(evt);
        check(a.count == 2 && b.count == 2, "second dispatch also to both");
        // add(null, null) -> null
        check(java.awt.AWTEventMulticaster.add((java.awt.event.ActionListener) null, null) == null,
              "add(null,null) -> null");
        // add(null, l) == l ; add(l, null) == l
        check(java.awt.AWTEventMulticaster.add((java.awt.event.ActionListener) null, b) == b, "add(null,l)==l");
        check(java.awt.AWTEventMulticaster.add(a, null) == a, "add(l,null)==l");
        // remove semantics
        java.awt.event.ActionListener rem = java.awt.AWTEventMulticaster.remove(chain, a);
        check(rem == b, "remove one leaves the other listener directly");
        rem.actionPerformed(evt);
        check(b.count == 3 && a.count == 2, "removed listener no longer notified");
        // remove from single listener not in chain -> returns same
        Counting c = new Counting("c");
        check(java.awt.AWTEventMulticaster.remove(c, a) == c, "remove absent listener -> same");
        // remove(null, l) -> null ; remove(l, null) -> l
        check(java.awt.AWTEventMulticaster.remove((java.awt.event.ActionListener) null, a) == null, "remove(null,l) -> null");
        check(java.awt.AWTEventMulticaster.remove(a, null) == a, "remove(l,null) -> l");
        // duplicate listener added twice, removed once still receives once
        Counting dup = new Counting("dup");
        java.awt.event.ActionListener twice = java.awt.AWTEventMulticaster.add(dup, dup);
        twice.actionPerformed(evt);
        check(dup.count == 2, "duplicate add notifies twice");
        java.awt.event.ActionListener once = java.awt.AWTEventMulticaster.remove(twice, dup);
        check(once != null, "remove one duplicate leaves registered (non-null)");
        once.actionPerformed(evt);
        check(dup.count == 3, "one duplicate removal leaves single registration");
        check(java.awt.AWTEventMulticaster.remove(once, dup) == null, "second removal empties chain");
        // three-deep chain removal of middle
        Counting l1 = new Counting("1"), l2 = new Counting("2"), l3 = new Counting("3");
        java.awt.event.ActionListener three = java.awt.AWTEventMulticaster.add(
                java.awt.AWTEventMulticaster.add(l1, l2), l3);
        java.awt.event.ActionListener two = java.awt.AWTEventMulticaster.remove(three, l2);
        two.actionPerformed(evt);
        check(l1.count == 1 && l2.count == 0 && l3.count == 1, "middle removal keeps outer two");
        // works for other listener types (ComponentListener family)
        java.awt.event.ComponentListener cl1 = new java.awt.event.ComponentAdapter() { };
        java.awt.event.ComponentListener cl2 = new java.awt.event.ComponentAdapter() { };
        java.awt.event.ComponentListener clChain = java.awt.AWTEventMulticaster.add(cl1, cl2);
        check(clChain != null, "component listener add");
        check(java.awt.AWTEventMulticaster.remove(clChain, cl1) == cl2, "component listener remove");
        // multicaster implements all AWT listener interfaces
        check(chain instanceof java.awt.event.ActionListener, "chain is ActionListener");
        check(chain instanceof java.awt.event.ComponentListener, "chain is ComponentListener");
        check(chain instanceof java.awt.event.KeyListener, "chain is KeyListener");
        check(chain instanceof java.awt.event.MouseListener, "chain is MouseListener");
        check(chain instanceof java.awt.event.MouseMotionListener, "chain is MouseMotionListener");
        check(chain instanceof java.awt.event.WindowListener, "chain is WindowListener");
        // exception propagation: listener throwing propagates to caller
        final java.lang.RuntimeException bomb = new java.lang.RuntimeException("bomb");
        java.awt.event.ActionListener thrower = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { throw bomb; }
        };
        java.awt.event.ActionListener withThrower = java.awt.AWTEventMulticaster.add(thrower, b);
        boolean propagated = false;
        try { withThrower.actionPerformed(evt); } catch (java.lang.RuntimeException re) { propagated = re == bomb; }
        check(propagated, "listener exception propagates with same object");
    }

}
