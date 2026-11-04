/*
 * @test
 * @summary java.awt.AWTEventMulticaster conformance: white-box structure + black-box behavior.
 * @run main AWTEventMulticasterTest
 */

import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.TextListener;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class AWTEventMulticasterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A listener that implements two unrelated listener interfaces.
    static class Rec implements ComponentListener, ActionListener {
        int componentResized, actionPerformed;
        public void componentResized(ComponentEvent e) { componentResized++; }
        public void componentMoved(ComponentEvent e) { }
        public void componentShown(ComponentEvent e) { }
        public void componentHidden(ComponentEvent e) { }
        public void actionPerformed(ActionEvent e) { actionPerformed++; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.AWTEventMulticaster");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AWTEventMulticaster is public");
        check(!Modifier.isAbstract(m), "AWTEventMulticaster is concrete");
        check(Arrays.asList(c.getInterfaces()).contains(ComponentListener.class), "implements ComponentListener");
        check(Arrays.asList(c.getInterfaces()).contains(ActionListener.class), "implements ActionListener");
        check(c.getDeclaredConstructor(java.util.EventListener.class, java.util.EventListener.class) != null, "has (EventListener,EventListener) ctor");
        check(Modifier.isStatic(c.getMethod("add", ComponentListener.class, ComponentListener.class).getModifiers()), "add(ComponentListener) static");
        check(Modifier.isStatic(c.getMethod("remove", ComponentListener.class, ComponentListener.class).getModifiers()), "remove(ComponentListener) static");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        ComponentEvent evt = new ComponentEvent(src, ComponentEvent.COMPONENT_RESIZED);
        ActionEvent aevt = new ActionEvent(src, ActionEvent.ACTION_PERFORMED, "go");

        Rec r1 = new Rec();
        Rec r2 = new Rec();

        // add identity / null handling
        check(AWTEventMulticaster.add((ComponentListener) null, (ComponentListener) null) == null, "add(null,null) == null");
        check(AWTEventMulticaster.add(r1, (ComponentListener) null) == r1, "add(l,null) == l");
        check(AWTEventMulticaster.add((ComponentListener) null, r2) == r2, "add(null,l) == l");

        // chain dispatch
        ComponentListener chain = AWTEventMulticaster.add((ComponentListener) r1, (ComponentListener) r2);
        check(chain != r1 && chain != r2, "add returns new multicaster");
        check(chain instanceof AWTEventMulticaster, "chain instanceof AWTEventMulticaster");
        chain.componentResized(evt);
        check(r1.componentResized == 1 && r2.componentResized == 1, "dispatch reaches both listeners");

        // remove
        check(AWTEventMulticaster.remove(chain, r1) == r2, "remove(chain,r1) == r2");
        check(AWTEventMulticaster.remove(chain, r2) == r1, "remove(chain,r2) == r1");
        check(AWTEventMulticaster.remove((ComponentListener) r1, (ComponentListener) r1) == null, "remove(l,l) == null");
        check(AWTEventMulticaster.remove(chain, new Rec()) == chain, "remove(unknown) == chain");

        // getListeners
        ComponentListener[] cls = AWTEventMulticaster.getListeners(chain, ComponentListener.class);
        check(cls.length == 2, "getListeners length 2");
        check(Arrays.asList(cls).contains(r1) && Arrays.asList(cls).contains(r2), "getListeners contains both");

        // type filtering: a Rec is both ComponentListener and ActionListener
        ComponentListener mixed = AWTEventMulticaster.add(r1, (ComponentListener) r2);
        ActionListener[] als = AWTEventMulticaster.getListeners(mixed, ActionListener.class);
        check(als.length == 2, "getListeners ActionListener finds both Rec (2)");
        TextListener[] tls = AWTEventMulticaster.getListeners(mixed, TextListener.class);
        check(tls.length == 0, "getListeners TextListener empty");

        // ActionListener dispatch
        ActionListener alChain = AWTEventMulticaster.add((ActionListener) r1, (ActionListener) r2);
        alChain.actionPerformed(aevt);
        check(r1.actionPerformed == 1 && r2.actionPerformed == 1, "actionPerformed dispatched to both");

        // three-way chain
        Rec r3 = new Rec();
        ComponentListener c3 = AWTEventMulticaster.add(chain, r3);
        c3.componentResized(evt);
        check(r1.componentResized == 2 && r2.componentResized == 2 && r3.componentResized == 1, "3-way chain dispatch");

        // remove from 3-way chain leaves a working 2-way chain
        ComponentListener back = AWTEventMulticaster.remove(c3, r3);
        back.componentResized(evt);
        check(r1.componentResized == 3 && r2.componentResized == 3 && r3.componentResized == 1, "remove from 3-way chain");

        // getListeners of a single (non-multicaster) listener
        ComponentListener[] single = AWTEventMulticaster.getListeners(r1, ComponentListener.class);
        check(single.length == 1 && single[0] == r1, "getListeners of single listener");
        ComponentListener[] empty = AWTEventMulticaster.getListeners(null, ComponentListener.class);
        check(empty.length == 0, "getListeners(null) empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
