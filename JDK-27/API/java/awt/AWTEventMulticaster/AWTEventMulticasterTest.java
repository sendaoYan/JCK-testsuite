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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
