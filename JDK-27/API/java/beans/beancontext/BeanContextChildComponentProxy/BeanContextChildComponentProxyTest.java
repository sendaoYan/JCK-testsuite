/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextChildComponentProxy conformance: white-box structure + black-box behavior.
 * @run main BeanContextChildComponentProxyTest
 */

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;

public class BeanContextChildComponentProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildComponentProxy");
        check(c.isInterface(), "is an interface");
        check(Component.class.isAssignableFrom(Component.class), "Component type");
    }

    static void testBlackBox() throws Exception {
        final Component comp = new javax.swing.JLabel();
        BeanContextChildComponentProxy p = new BeanContextChildComponentProxy() {
            public Component getComponent() { return comp; }
        };
        check(comp == p.getComponent(), "getComponent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
