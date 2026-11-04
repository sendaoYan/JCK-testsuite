/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServiceRevokedListener conformance: white-box structure + black-box behavior.
 * @run main BeanContextServiceRevokedListenerTest
 */

import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServiceRevokedListener;
import java.beans.beancontext.BeanContextServicesSupport;

public class BeanContextServiceRevokedListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceRevokedListener");
        check(c.isInterface(), "is an interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
    }

    static void testBlackBox() throws Exception {
        final int[] n = new int[1];
        BeanContextServiceRevokedListener l = new BeanContextServiceRevokedListener() {
            public void serviceRevoked(BeanContextServiceRevokedEvent e) { n[0]++; }
        };
        l.serviceRevoked(new BeanContextServiceRevokedEvent(new BeanContextServicesSupport(), Object.class, true));
        check(n[0] == 1, "serviceRevoked dispatch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
