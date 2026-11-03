/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServiceProvider conformance: white-box structure + black-box behavior.
 * @run main BeanContextServiceProviderTest
 */

import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.util.Collections;
import java.util.Iterator;

public class BeanContextServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceProvider");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final Object svc = new Object();
        BeanContextServiceProvider p = new BeanContextServiceProvider() {
            public Object getService(BeanContextServices bcs, Object requestor, Class<?> serviceClass, Object selector) { return svc; }
            public void releaseService(BeanContextServices bcs, Object requestor, Object service) {}
            public Iterator<?> getCurrentServiceSelectors(BeanContextServices bcs, Class<?> serviceClass) { return Collections.emptyList().iterator(); }
        };
        BeanContextServices bcs = new BeanContextServicesSupport();
        check(svc == p.getService(bcs, null, Object.class, null), "getService returns service");
        p.releaseService(bcs, null, svc);
        check(p.getCurrentServiceSelectors(bcs, Object.class) != null, "selectors non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
