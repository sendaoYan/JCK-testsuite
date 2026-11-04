/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextEvent conformance: white-box structure + black-box behavior.
 * @run main BeanContextEventTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextEvent;
import java.beans.beancontext.BeanContextSupport;

public class BeanContextEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class E extends BeanContextEvent { E(BeanContext bc) { super(bc); } }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextEvent");
        check(java.util.EventObject.class.isAssignableFrom(c), "extends EventObject");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
    }

    static void testBlackBox() throws Exception {
        BeanContext bc = new BeanContextSupport();
        E e = new E(bc);
        check(bc == e.getBeanContext(), "getBeanContext");
        check(e.getPropagatedFrom() == null, "propagatedFrom null default");
        check(!e.isPropagated(), "not propagated default");
        BeanContext bc2 = new BeanContextSupport();
        e.setPropagatedFrom(bc2);
        check(bc2 == e.getPropagatedFrom(), "propagatedFrom set");
        check(e.isPropagated(), "isPropagated true");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
