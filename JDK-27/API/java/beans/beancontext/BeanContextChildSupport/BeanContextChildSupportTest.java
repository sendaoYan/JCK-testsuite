/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextChildSupport conformance: white-box structure + black-box behavior.
 * @run main BeanContextChildSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesSupport;
import java.beans.beancontext.BeanContextSupport;

public class BeanContextChildSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildSupport");
        check(BeanContextChild.class.isAssignableFrom(c), "implements BeanContextChild");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testBlackBox() throws Exception {
        BeanContextChildSupport cs = new BeanContextChildSupport();
        check(cs.getBeanContext() == null, "beanContext null default");
        check(cs.getBeanContextChildPeer() == cs, "peer is self");
        check(!cs.isDelegated(), "not delegated default");

        BeanContextSupport bc = new BeanContextSupport();
        cs.setBeanContext(bc);
        check(cs.getBeanContext() == bc, "setBeanContext");
        check(cs.validatePendingSetBeanContext(bc), "validatePendingSetBeanContext true");

        final int[] p = new int[1];
        cs.addPropertyChangeListener("x", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { p[0]++; }
        });
        cs.firePropertyChange("x", null, null);
        check(p[0] == 1, "firePropertyChange");

        cs.addVetoableChangeListener("y", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) {}
        });
        cs.fireVetoableChange("y", null, null);
        check(true, "fireVetoableChange no throw");

        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        cs.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, true));
        cs.serviceAvailable(new BeanContextServiceAvailableEvent(bcss, Object.class));
        check(true, "serviceRevoked/serviceAvailable no throw");

        BeanContextChildSupport peer = new BeanContextChildSupport();
        BeanContextChildSupport cs2 = new BeanContextChildSupport(peer);
        check(cs2.getBeanContextChildPeer() == peer, "peer ctor");
        check(cs2.isDelegated(), "delegated ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
