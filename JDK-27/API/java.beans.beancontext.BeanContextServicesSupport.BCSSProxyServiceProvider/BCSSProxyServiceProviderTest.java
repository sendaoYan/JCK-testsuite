/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSProxyServiceProvider conformance: white-box structure + black-box behavior.
 * @run main BCSSProxyServiceProviderTest
 */

import java.lang.reflect.Modifier;

public class BCSSProxyServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider");
        int m = c.getModifiers();
        check(Modifier.isProtected(m), "protected class");
        check(java.beans.beancontext.BeanContextServiceProvider.class.isAssignableFrom(c), "implements BeanContextServiceProvider");
        check(java.beans.beancontext.BeanContextServiceRevokedListener.class.isAssignableFrom(c), "implements BeanContextServiceRevokedListener");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getService") && names.contains("releaseService") &&
              names.contains("getCurrentServiceSelectors") && names.contains("serviceRevoked"), "declares service methods");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
