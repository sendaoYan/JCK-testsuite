/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServicesListener conformance: white-box structure + black-box behavior.
 * @run main BeanContextServicesListenerTest
 */

import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesListener;
import java.beans.beancontext.BeanContextServicesSupport;

public class BeanContextServicesListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesListener");
        check(c.isInterface(), "is an interface");
        check(java.beans.beancontext.BeanContextServiceRevokedListener.class.isAssignableFrom(c), "extends BeanContextServiceRevokedListener");
    }

    static void testBlackBox() throws Exception {
        final int[] a = new int[1], r = new int[1];
        BeanContextServicesListener l = new BeanContextServicesListener() {
            public void serviceAvailable(BeanContextServiceAvailableEvent e) { a[0]++; }
            public void serviceRevoked(BeanContextServiceRevokedEvent e) { r[0]++; }
        };
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        l.serviceAvailable(new BeanContextServiceAvailableEvent(bcss, Object.class));
        l.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, true));
        check(a[0] == 1 && r[0] == 1, "serviceAvailable/serviceRevoked dispatch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
