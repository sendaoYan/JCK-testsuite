/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextSupport.BCSChild conformance: white-box structure + black-box behavior.
 * @run main BCSChildTest
 */

import java.lang.reflect.Modifier;

public class BCSChildTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport$BCSChild");
        int m = c.getModifiers();
        check(Modifier.isProtected(m), "protected class");
        check(!Modifier.isPublic(m), "not public");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
        java.lang.reflect.Constructor<?> ct = c.getDeclaredConstructors()[0];
        check(!Modifier.isPublic(ct.getModifiers()) && !Modifier.isProtected(ct.getModifiers()), "ctor package-private");
    }

    static void testBlackBox() throws Exception {
        java.beans.beancontext.BeanContextSupport bc = new java.beans.beancontext.BeanContextSupport();
        check(bc.add("kid"), "add creates BCSChild internally");
        check(bc.size() == 1 && bc.contains("kid"), "child tracked");
        check("kid".equals(bc.iterator().next()), "iterator yields child");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
