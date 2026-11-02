/*
 * @test
 * @summary java.beans.beancontext.BeanContextChildSupport conformance: type shape.
 * @run main BeanContextChildSupportTest
 */

import java.lang.reflect.Modifier;

public class BeanContextChildSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildSupport");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 3, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.beans.beancontext.BeanContextChild"), "implements java.beans.beancontext.BeanContextChild");
        check(got.contains("java.beans.beancontext.BeanContextServicesListener"), "implements java.beans.beancontext.BeanContextServicesListener");
        check(got.contains("java.io.Serializable"), "implements java.io.Serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
