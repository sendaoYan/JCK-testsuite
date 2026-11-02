/*
 * @test
 * @summary java.beans.beancontext.BeanContextSupport conformance: type shape.
 * @run main BeanContextSupportTest
 */

import java.lang.reflect.Modifier;

public class BeanContextSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.beans.beancontext.BeanContextChildSupport"), "superclass");
        check(c.getInterfaces().length == 4, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.beans.PropertyChangeListener"), "implements java.beans.PropertyChangeListener");
        check(got.contains("java.beans.VetoableChangeListener"), "implements java.beans.VetoableChangeListener");
        check(got.contains("java.beans.beancontext.BeanContext"), "implements java.beans.beancontext.BeanContext");
        check(got.contains("java.io.Serializable"), "implements java.io.Serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
