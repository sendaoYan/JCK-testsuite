/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextMembershipEvent conformance: white-box structure + black-box behavior.
 * @run main BeanContextMembershipEventTest
 */

import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextSupport;
import java.util.Arrays;
import java.util.Iterator;

public class BeanContextMembershipEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextMembershipEvent");
        check(java.beans.beancontext.BeanContextEvent.class.isAssignableFrom(c), "extends BeanContextEvent");
    }

    static void testBlackBox() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        Object[] kids = {"a", "b"};
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(bc, kids);
        check(bc == e.getBeanContext(), "getBeanContext");
        check(e.size() == 2, "size");
        check(e.contains("a") && e.contains("b"), "contains");
        check(Arrays.equals(new Object[]{"a","b"}, e.toArray()), "toArray");
        int n = 0; for (Iterator it = e.iterator(); it.hasNext(); it.next()) n++;
        check(n == 2, "iterator");

        BeanContextMembershipEvent e2 = new BeanContextMembershipEvent(bc, Arrays.asList(kids));
        check(e2.size() == 2, "Collection ctor size");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
