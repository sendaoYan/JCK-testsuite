/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextMembershipListener conformance: white-box structure + black-box behavior.
 * @run main BeanContextMembershipListenerTest
 */

import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextSupport;

public class BeanContextMembershipListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextMembershipListener");
        check(c.isInterface(), "is an interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
    }

    static void testBlackBox() throws Exception {
        final int[] a = new int[1], r = new int[1];
        BeanContextMembershipListener l = new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { a[0]++; }
            public void childrenRemoved(BeanContextMembershipEvent e) { r[0]++; }
        };
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(new BeanContextSupport(), new Object[]{"x"});
        l.childrenAdded(e);
        l.childrenRemoved(e);
        check(a[0] == 1 && r[0] == 1, "childrenAdded/childrenRemoved dispatch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
