/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextServicesSupport.BCSSChild conformance: white-box structure + black-box behavior.
 * @run main BCSSChildTest
 */

import java.lang.reflect.Modifier;

public class BCSSChildTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport$BCSSChild");
        int m = c.getModifiers();
        check(Modifier.isProtected(m), "protected class");
        check("java.beans.beancontext.BeanContextSupport$BCSChild".equals(c.getSuperclass().getName()), "extends BCSChild");
    }

    static void testBlackBox() throws Exception {
        java.beans.beancontext.BeanContextServicesSupport bcss = new java.beans.beancontext.BeanContextServicesSupport();
        check(bcss.add("kid"), "add creates BCSSChild internally");
        check(bcss.size() == 1 && bcss.contains("kid"), "child tracked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
