/*
 * @test
 * @summary java.beans.beancontext.BeanContextMembershipListener conformance: white-box structure + black-box behavior.
 *          Exact interface inventory (exactly 2 methods childrenAdded/childrenRemoved,
 *          both taking BeanContextMembershipEvent and returning void, extends
 *          EventListener, no defaults in the JDK interface), plus full delivery
 *          semantics driven by a real BeanContextSupport flow: childrenAdded on add with
 *          the exact child, childrenRemoved on remove, event source/contents assertions,
 *          duplicate registration is idempotent, removal stops delivery, both listeners
 *          notified, no events on failed (vetoed/rejected) adds, and listener
 *          RuntimeException propagates to the mutating caller.
 * @run main BeanContextMembershipListenerTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeanContextMembershipListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static class Recorder implements BeanContextMembershipListener {
        int added, removed;
        List<BeanContextMembershipEvent> addedEvents = new ArrayList<BeanContextMembershipEvent>();
        List<BeanContextMembershipEvent> removedEvents = new ArrayList<BeanContextMembershipEvent>();
        public void childrenAdded(BeanContextMembershipEvent e) { added++; addedEvents.add(e); }
        public void childrenRemoved(BeanContextMembershipEvent e) { removed++; removedEvents.add(e); }
    }

    static class VetoChild extends BeanContextChildSupport {
        VetoChild() {
            addVetoableChangeListener("beanContext", new VetoableChangeListener() {
                public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                    if (e.getNewValue() != null) throw new PropertyVetoException("veto", e);
                }
            });
        }
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextMembershipListener");
        check(c.isInterface(), "is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
        // exactly two methods
        List<String> names = new ArrayList<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        java.util.Collections.sort(names);
        check(names.size() == 2, "declares exactly 2 methods: " + names);
        check(names.equals(Arrays.asList("childrenAdded", "childrenRemoved")), "exact method names");
        check(hasMethod(c, "childrenAdded"), "declares childrenAdded");
        check(hasMethod(c, "childrenRemoved"), "declares childrenRemoved");
        // both take BeanContextMembershipEvent and return void
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            Class<?>[] p = m.getParameterTypes();
            check(p.length == 1 && p[0].getName().equals("java.beans.beancontext.BeanContextMembershipEvent"),
                  m.getName() + " takes a BeanContextMembershipEvent");
            check(m.getReturnType() == void.class, m.getName() + " returns void");
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), m.getName() + " is abstract");
        }
    }

    static void testDelivery() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        final Recorder rec = new Recorder();
        bc.addBeanContextMembershipListener(rec);
        // add delivers childrenAdded with the exact child
        Object kid = new Object();
        bc.add(kid);
        check(rec.added == 1 && rec.removed == 0, "childrenAdded delivered on add");
        BeanContextMembershipEvent ev = rec.addedEvents.get(0);
        check(ev.getBeanContext() == bc, "delivered event source is the context");
        check(ev.getSource() == bc, "getSource is the context");
        check(ev.size() == 1, "event carries exactly one child");
        check(ev.contains(kid) && ev.toArray()[0] == kid, "event carries the exact child instance");
        check(ev.getPropagatedFrom() == null && !ev.isPropagated(), "fresh event not propagated");
        // remove delivers childrenRemoved
        bc.remove(kid);
        check(rec.removed == 1, "childrenRemoved delivered on remove");
        check(rec.removedEvents.get(0).toArray()[0] == kid, "childrenRemoved carries the child");
        // duplicate registration is idempotent
        bc.addBeanContextMembershipListener(rec);
        bc.add("dup");
        check(rec.added == 2, "duplicate registration still fires once");
        // removal stops delivery
        bc.removeBeanContextMembershipListener(rec);
        bc.add("quiet");
        check(rec.added == 2, "removed listener gets nothing");
        // two listeners both notified
        Recorder r1 = new Recorder();
        Recorder r2 = new Recorder();
        bc.addBeanContextMembershipListener(r1);
        bc.addBeanContextMembershipListener(r2);
        bc.add("both");
        check(r1.added == 1 && r2.added == 1, "both registered listeners notified");
        bc.remove("both");
        check(r1.removed == 1 && r2.removed == 1, "both notified on removal");
        // no events on failed operations
        BeanContextSupport bcf = new BeanContextSupport();
        Recorder rf = new Recorder();
        bcf.addBeanContextMembershipListener(rf);
        check(bcf.add("dup"), "first add returns true");
        check(rf.added == 1, "successful add fired");
        check(!bcf.add("dup"), "duplicate add returns false");
        check(rf.added == 1, "duplicate add fires nothing");
        check(!bcf.remove("absent"), "absent remove returns false");
        check(rf.removed == 0, "absent remove fires nothing");
        try {
            bcf.add(new VetoChild());
            check(false, "vetoing child add must fail");
        } catch (IllegalStateException ise) {
            check(true, "vetoing child add -> IllegalStateException");
        }
        check(rf.added == 1, "no childrenAdded when the add failed");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextSupport().add(null); } }, "add(null) -> IAE, no listener involved");
        check(rf.added == 1 && rf.removed == 0, "failed add fires no events");
    }

    static void testExceptionPropagation() throws Exception {
        // a listener throwing inside childrenAdded propagates to the add() caller
        BeanContextSupport bc = new BeanContextSupport();
        final Recorder after = new Recorder();
        bc.addBeanContextMembershipListener(new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { throw new RuntimeException("boomAdd"); }
            public void childrenRemoved(BeanContextMembershipEvent e) { }
        });
        bc.addBeanContextMembershipListener(after);
        try {
            bc.add("x");
            check(false, "throwing childrenAdded must propagate");
        } catch (RuntimeException re) {
            check("boomAdd".equals(re.getMessage()), "childrenAdded exception propagates to add()");
        }
        check(bc.contains("x"), "child was added before the exception");
        check(after.added == 0, "later listeners skipped after the throw");
        // same for childrenRemoved from remove()
        BeanContextSupport bc2 = new BeanContextSupport();
        bc2.addBeanContextMembershipListener(new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { }
            public void childrenRemoved(BeanContextMembershipEvent e) { throw new RuntimeException("boomRem"); }
        });
        bc2.add("y");
        try {
            bc2.remove("y");
            check(false, "throwing childrenRemoved must propagate");
        } catch (RuntimeException re) {
            check("boomRem".equals(re.getMessage()), "childrenRemoved exception propagates to remove()");
        }
        check(!bc2.contains("y"), "child was removed before the exception");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testDelivery();
        testExceptionPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
