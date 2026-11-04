/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSProxyServiceProvider conformance: concrete class reflection.
 * @run main BCSSProxyServiceProviderTest
 */

public class BCSSProxyServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.beans.beancontext.BeanContextServiceProvider").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServiceProvider");
        check(Class.forName("java.beans.beancontext.BeanContextServiceRevokedListener").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServiceRevokedListener");
        check(hasMethod(c, "getCurrentServiceSelectors"), "declares getCurrentServiceSelectors");
        check(hasMethod(c, "getService"), "declares getService");
        check(hasMethod(c, "releaseService"), "declares releaseService");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
