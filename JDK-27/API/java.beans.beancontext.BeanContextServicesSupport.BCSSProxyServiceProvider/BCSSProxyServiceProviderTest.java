/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSProxyServiceProvider conformance: type shape.
 * @run main BCSSProxyServiceProviderTest
 */

import java.lang.reflect.Modifier;

public class BCSSProxyServiceProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSProxyServiceProvider");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == false, "public");
        check(Modifier.isProtected(m) == true, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 2, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.beans.beancontext.BeanContextServiceProvider"), "implements java.beans.beancontext.BeanContextServiceProvider");
        check(got.contains("java.beans.beancontext.BeanContextServiceRevokedListener"), "implements java.beans.beancontext.BeanContextServiceRevokedListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
