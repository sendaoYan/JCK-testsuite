/*
 * @test
 * @summary java.beans.beancontext.BeanContext conformance: type shape.
 * @run main BeanContextTest
 */

import java.lang.reflect.Modifier;

public class BeanContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContext");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == true, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == true, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 4, "interface count");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.beans.DesignMode"), "implements java.beans.DesignMode");
        check(got.contains("java.beans.Visibility"), "implements java.beans.Visibility");
        check(got.contains("java.beans.beancontext.BeanContextChild"), "implements java.beans.beancontext.BeanContextChild");
        check(got.contains("java.util.Collection"), "implements java.util.Collection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
