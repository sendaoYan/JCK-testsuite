/*
 * @test
 * @summary java.beans.VetoableChangeSupport conformance: white-box structure + black-box behavior.
 * @run main VetoableChangeSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

public class VetoableChangeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.VetoableChangeSupport");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testBlackBox() throws Exception {
        Object source = new Object();
        VetoableChangeSupport vcs = new VetoableChangeSupport(source);
        check(!vcs.hasListeners("foo"), "no listeners initially");

        final int[] named = new int[1];
        vcs.addVetoableChangeListener("foo", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { named[0]++; }
        });
        check(vcs.hasListeners("foo"), "hasListeners after add");
        check(vcs.getVetoableChangeListeners("foo").length == 1, "named listeners");

        vcs.fireVetoableChange("foo", "a", "b");
        check(named[0] == 1, "fire named");

        vcs.removeVetoableChangeListener("foo", vcs.getVetoableChangeListeners("foo")[0]);
        check(!vcs.hasListeners("foo"), "hasListeners after remove");

        vcs.addVetoableChangeListener("bar", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) { try { throw new PropertyVetoException("veto", e); } catch (PropertyVetoException ex) { /* rethrow */ } }
        });
        vcs.addVetoableChangeListener("bar", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                throw new PropertyVetoException("veto", e);
            }
        });
        try {
            vcs.fireVetoableChange("bar", "x", "y");
            check(false, "veto should propagate");
        } catch (PropertyVetoException pve) {
            check("bar".equals(pve.getPropertyChangeEvent().getPropertyName()), "veto event property");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
