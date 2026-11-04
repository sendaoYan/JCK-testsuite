/*
 * @test
 * @summary java.beans.VetoableChangeSupport conformance: veto throws PropertyVetoException.
 * @run main VetoableChangeSupportTest
 */

import java.beans.*;
public class VetoableChangeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        VetoableChangeSupport vcs = new VetoableChangeSupport(new Object());
        vcs.addVetoableChangeListener(new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                throw new PropertyVetoException("veto", e);
            }
        });
        boolean vetoed = false;
        try {
            vcs.fireVetoableChange("p", "old", "new");
        } catch (PropertyVetoException ex) {
            vetoed = true;
        }
        check(vetoed, "veto propagated");
        check(vcs.hasListeners("p"), "hasListeners");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
