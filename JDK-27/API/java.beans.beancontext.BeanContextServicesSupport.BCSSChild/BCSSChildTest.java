/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSChild conformance: type shape.
 * @run main BCSSChildTest
 */

import java.lang.reflect.Modifier;

public class BCSSChildTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSChild");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == false, "public");
        check(Modifier.isProtected(m) == true, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.beans.beancontext.BeanContextSupport$BCSChild"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
