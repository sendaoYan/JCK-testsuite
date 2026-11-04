/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServiceAvailableEvent conformance: white-box structure + black-box behavior.
 * @run main BeanContextServiceAvailableEventTest
 */

import java.beans.beancontext.BeanContextServiceAvailableEvent;
import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServicesSupport;
import java.util.Collections;
import java.util.Iterator;

public class BeanContextServiceAvailableEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceAvailableEvent");
        check(java.beans.beancontext.BeanContextEvent.class.isAssignableFrom(c), "extends BeanContextEvent");
    }

    static void testBlackBox() throws Exception {
        BeanContextServicesSupport bcss = new BeanContextServicesSupport();
        bcss.addService(Object.class, new BeanContextServiceProvider() {
            public Object getService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Class<?> serviceClass, Object selector) { return new Object(); }
            public void releaseService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Object service) {}
            public Iterator<?> getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices bcs, Class<?> serviceClass) { return Collections.emptyList().iterator(); }
        });
        BeanContextServiceAvailableEvent e = new BeanContextServiceAvailableEvent(bcss, Object.class);
        check(bcss == e.getSourceAsBeanContextServices(), "getSourceAsBeanContextServices");
        check(Object.class == e.getServiceClass(), "getServiceClass");
        check(bcss == e.getBeanContext(), "getBeanContext");
        check(e.getCurrentServiceSelectors() != null, "getCurrentServiceSelectors non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
