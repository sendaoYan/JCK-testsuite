/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServiceRevokedEvent conformance: white-box structure + black-box behavior.
 * @run main BeanContextServiceRevokedEventTest
 */

import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextServicesSupport;

public class BeanContextServiceRevokedEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceRevokedEvent");
        check(java.beans.beancontext.BeanContextEvent.class.isAssignableFrom(c), "extends BeanContextEvent");
    }

    static void testBlackBox() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        BeanContextServiceRevokedEvent e = new BeanContextServiceRevokedEvent(bcss, Object.class, true);
        check(bcss == e.getSourceAsBeanContextServices(), "getSourceAsBeanContextServices");
        check(Object.class == e.getServiceClass(), "getServiceClass");
        check(e.isServiceClass(Object.class), "isServiceClass true");
        check(!e.isServiceClass(String.class), "isServiceClass false");
        check(e.isCurrentServiceInvalidNow(), "invalidNow true");
        BeanContextServiceRevokedEvent e2 = new BeanContextServiceRevokedEvent(bcss, Object.class, false);
        check(!e2.isCurrentServiceInvalidNow(), "invalidNow false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
