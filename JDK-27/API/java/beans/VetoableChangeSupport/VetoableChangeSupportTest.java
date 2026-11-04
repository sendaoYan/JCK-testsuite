/*
 * @test
 * @summary java.beans.VetoableChangeSupport conformance: full contract.
 *          null source -> NPE, null listener / null name no-op semantics,
 *          global vs named listener registration, exact fire/no-fire
 *          conditions for Object/int/boolean, fireVetoableChange(event)
 *          condition, and PropertyVetoException propagation (a vetoing
 *          listener aborts delivery and surfaces the event).
 * @run main VetoableChangeSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

public class VetoableChangeSupportTest {
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

    static class Counting implements VetoableChangeListener {
        int count;
        PropertyChangeEvent last;
        public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException { count++; last = e; }
    }

    static void testConstructorAndNoOp() {
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() {
                    new VetoableChangeSupport(null);
                }}, "null source -> NPE");

        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        check(!vcs.hasListeners("foo"), "no listeners initially");
        vcs.addVetoableChangeListener((VetoableChangeListener) null);
        check(vcs.getVetoableChangeListeners().length == 0, "add(null) no-op");
        vcs.addVetoableChangeListener("foo", (VetoableChangeListener) null);
        check(vcs.getVetoableChangeListeners("foo").length == 0, "add(foo,null) no-op");
        vcs.addVetoableChangeListener((String) null, new Counting());
        check(vcs.getVetoableChangeListeners().length == 0, "add(null name) no-op");
        check(vcs.getVetoableChangeListeners(null).length == 0, "getListeners(null) empty");
    }

    static void testGlobalVsNamed() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting global = new Counting();
        Counting named = new Counting();
        vcs.addVetoableChangeListener("foo", named);
        check(vcs.hasListeners("foo"), "hasListeners foo after named add");
        check(!vcs.hasListeners("bar"), "hasListeners bar false with only named foo listener");

        vcs.addVetoableChangeListener(global);
        check(vcs.hasListeners("bar"), "global listener counts for any name");
        check(vcs.getVetoableChangeListeners().length == 2, "global + named count");
        check(vcs.getVetoableChangeListeners("foo").length == 1, "named count");

        vcs.fireVetoableChange("foo", "a", "b");
        check(global.count == 1 && named.count == 1, "fire foo hits global + named");
        check(src == global.last.getSource(), "event source");
        check("foo".equals(global.last.getPropertyName()), "event property");

        vcs.fireVetoableChange("bar", 1, 2);
        check(global.count == 2 && named.count == 1, "fire bar only global");

        vcs.removeVetoableChangeListener("foo", named);
        vcs.fireVetoableChange("foo", "x", "y");
        check(global.count == 3 && named.count == 1, "removed named not fired");
    }

    static void testFireConditions() throws Exception {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        Counting c = new Counting();
        vcs.addVetoableChangeListener(c);

        vcs.fireVetoableChange("p", "a", "a");
        check(c.count == 0, "equal non-null objects not fired");
        vcs.fireVetoableChange("p", null, null);
        check(c.count == 1, "both null fires");
        vcs.fireVetoableChange("p", 5, 5);
        check(c.count == 1, "equal int not fired");
        vcs.fireVetoableChange("p", 5, 6);
        check(c.count == 2, "different int fired");
        vcs.fireVetoableChange("p", true, true);
        check(c.count == 2, "equal boolean not fired");
        vcs.fireVetoableChange("p", false, true);
        check(c.count == 3, "different boolean fired");

        vcs.fireVetoableChange(new PropertyChangeEvent(src, "p", "same", "same"));
        check(c.count == 3, "equal non-null event not fired");
        vcs.fireVetoableChange(new PropertyChangeEvent(src, "p", null, "v"));
        check(c.count == 4, "null-old event fired");
    }

    static void testVetoPropagation() {
        Object src = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(src);
        final Counting after = new Counting();
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                throw new PropertyVetoException("veto", e);
            }
        });
        vcs.addVetoableChangeListener(after);
        try {
            vcs.fireVetoableChange("bar", "x", "y");
            check(false, "veto should throw");
        } catch (PropertyVetoException pve) {
            passed++;
            check("veto".equals(pve.getMessage()), "veto message");
            check("bar".equals(pve.getPropertyChangeEvent().getPropertyName()), "veto event property");
            check("x".equals(pve.getPropertyChangeEvent().getOldValue()), "veto event old value");
        }
        check(after.count == 0, "veto aborts subsequent listeners");
    }


    static void testStructure() {
        check(java.io.Serializable.class.isAssignableFrom(VetoableChangeSupport.class), "implements Serializable");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testConstructorAndNoOp();
        testGlobalVsNamed();
        testFireConditions();
        testVetoPropagation();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
