/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextChild conformance: white-box structure + black-box behavior.
 * @run main BeanContextChildTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;

public class BeanContextChildTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChild");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        BeanContextChildSupport child = new BeanContextChildSupport();
        check(child.getBeanContext() == null, "beanContext null default");
        BeanContextSupport bc = new BeanContextSupport();
        child.setBeanContext(bc);
        check(child.getBeanContext() == bc, "setBeanContext/getBeanContext");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
