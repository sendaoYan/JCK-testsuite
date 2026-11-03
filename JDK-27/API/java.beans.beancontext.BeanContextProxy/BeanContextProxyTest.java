/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextProxy conformance: white-box structure + black-box behavior.
 * @run main BeanContextProxyTest
 */

import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextProxy;

public class BeanContextProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextProxy");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final BeanContextChild peer = new BeanContextChildSupport();
        BeanContextProxy p = new BeanContextProxy() {
            public BeanContextChild getBeanContextProxy() { return peer; }
        };
        check(peer == p.getBeanContextProxy(), "getBeanContextProxy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
