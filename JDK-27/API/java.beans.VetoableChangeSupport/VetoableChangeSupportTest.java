/*
 * @test
 * @summary java.beans.VetoableChangeSupport conformance: fire and veto.
 * @run main VetoableChangeSupportTest
 */

import java.beans.*;

public class VetoableChangeSupportTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        VetoableChangeSupport vcs = new VetoableChangeSupport(new Object());
        final boolean[] fired = new boolean[1];
        vcs.addVetoableChangeListener("name", evt -> fired[0] = true);
        check(vcs.hasListeners("name"), "has listeners");
        vcs.fireVetoableChange("name", "old", "new");
        check(fired[0], "fired");
        check(vcs.getVetoableChangeListeners().length >= 1, "listeners");
        vcs.addVetoableChangeListener(evt -> { throw new PropertyVetoException("no", evt); });
        try { vcs.fireVetoableChange("x", 0, 1); check(false, "should veto"); }
        catch (PropertyVetoException expected) { check("no".equals(expected.getMessage()), "veto message"); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
