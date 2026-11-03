/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServices conformance: white-box structure + black-box behavior.
 * @run main BeanContextServicesTest
 */

import java.beans.beancontext.BeanContextServiceProvider;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.util.Collections;
import java.util.Iterator;

public class BeanContextServicesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class Svc implements BeanContextServiceProvider {
        final Object svc = new Object();
        public Object getService(BeanContextServices bcs, Object requestor, Class<?> serviceClass, Object selector) { return svc; }
        public void releaseService(BeanContextServices bcs, Object requestor, Object service) {}
        public Iterator<?> getCurrentServiceSelectors(BeanContextServices bcs, Class<?> serviceClass) { return Collections.emptyList().iterator(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServices");
        check(c.isInterface(), "is an interface");
        check(java.beans.beancontext.BeanContext.class.isAssignableFrom(c), "extends BeanContext");
    }

    static void testBlackBox() throws Exception {
        BeanContextServices bcs = new BeanContextServicesSupport();
        Svc sp = new Svc();
        check(bcs.addService(Object.class, sp), "addService returns true");
        check(bcs.hasService(Object.class), "hasService");
        Iterator<?> classes = bcs.getCurrentServiceClasses();
        boolean found = false; while (classes.hasNext()) if (classes.next() == Object.class) found = true;
        check(found, "getCurrentServiceClasses contains Object.class");
        bcs.revokeService(Object.class, sp, true);
        check(!bcs.hasService(Object.class), "revoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
