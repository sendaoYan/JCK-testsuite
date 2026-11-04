/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextContainerProxy conformance: white-box structure + black-box behavior.
 * @run main BeanContextContainerProxyTest
 */

import java.awt.Container;
import java.beans.beancontext.BeanContextContainerProxy;

public class BeanContextContainerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextContainerProxy");
        check(c.isInterface(), "is an interface");
    }

    static void testBlackBox() throws Exception {
        final Container cont = new java.awt.Panel();
        BeanContextContainerProxy p = new BeanContextContainerProxy() {
            public Container getContainer() { return cont; }
        };
        check(cont == p.getContainer(), "getContainer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
