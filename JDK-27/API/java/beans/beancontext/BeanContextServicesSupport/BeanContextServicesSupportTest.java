/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServicesSupport conformance: white-box structure + black-box behavior.
 * @run main BeanContextServicesSupportTest
 */

import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServiceRevokedEvent;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextServicesSupport;
import java.util.Collections;
import java.util.Iterator;

public class BeanContextServicesSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class Svc implements BeanContextServiceProvider {
        final Object svc = new Object();
        public Object getService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Class<?> serviceClass, Object selector) { return svc; }
        public void releaseService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Object service) {}
        public Iterator<?> getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices bcs, Class<?> serviceClass) { return Collections.emptyList().iterator(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport");
        check(java.beans.beancontext.BeanContextServices.class.isAssignableFrom(c), "implements BeanContextServices");
        check(java.beans.beancontext.BeanContextSupport.class.isAssignableFrom(c), "extends BeanContextSupport");
    }

    static void testBlackBox() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        check(bcss.getBeanContextServicesPeer() == bcss, "peer is self");

        Svc sp = new Svc();
        check(bcss.addService(Object.class, sp), "addService returns true");
        check(bcss.hasService(Object.class), "hasService");

        Iterator<?> classes = bcss.getCurrentServiceClasses();
        boolean found = false; while (classes.hasNext()) if (classes.next() == Object.class) found = true;
        check(found, "getCurrentServiceClasses contains Object.class");
        check(bcss.getCurrentServiceSelectors(Object.class) != null, "getCurrentServiceSelectors non-null");

        BeanContextChildSupport child = new BeanContextChildSupport();
        bcss.add(child);
        java.beans.beancontext.BeanContextServiceRevokedListener noop = new java.beans.beancontext.BeanContextServiceRevokedListener() {
            public void serviceRevoked(java.beans.beancontext.BeanContextServiceRevokedEvent e) {}
        };
        Object req = new Object();
        Object svc = bcss.getService(child, req, Object.class, null, noop);
        check(svc == sp.svc, "getService returns provider service");
        bcss.releaseService(child, req, svc);
        check(true, "releaseService no throw");

        bcss.serviceAvailable(new BeanContextServiceAvailableEvent(bcss, Object.class));
        bcss.serviceRevoked(new BeanContextServiceRevokedEvent(bcss, Object.class, true));
        check(true, "serviceAvailable/serviceRevoked no throw");

        bcss.revokeService(Object.class, sp, true);
        check(!bcss.hasService(Object.class), "revoked");

        BeanContextServicesSupport bcss2 = new BeanContextServicesSupport(bcss);
        check(bcss2.getBeanContextServicesPeer() == bcss, "nested ctor peer returns parent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
