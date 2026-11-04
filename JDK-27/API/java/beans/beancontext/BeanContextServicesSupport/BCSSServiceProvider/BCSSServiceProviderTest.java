/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSServiceProvider conformance: white-box structure + black-box behavior.
 * @run main BCSSServiceProviderTest
 */

import java.lang.reflect.Modifier;

public class BCSSServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSServiceProvider");
        int m = c.getModifiers();
        check(Modifier.isProtected(m), "protected class");
        check(Modifier.isStatic(m), "static class");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testBlackBox() throws Exception {
        java.beans.beancontext.BeanContextServicesSupport bcss = new java.beans.beancontext.BeanContextServicesSupport();
        java.beans.beancontext.BeanContextServiceProvider p = new java.beans.beancontext.BeanContextServiceProvider() {
            public Object getService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Class<?> serviceClass, Object selector) { return new Object(); }
            public void releaseService(java.beans.beancontext.BeanContextServices bcs, Object requestor, Object service) {}
            public java.util.Iterator<?> getCurrentServiceSelectors(java.beans.beancontext.BeanContextServices bcs, Class<?> serviceClass) { return null; }
        };
        check(bcss.addService(Object.class, p), "addService creates BCSSServiceProvider internally");
        check(bcss.hasService(Object.class), "hasService");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
