/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContext conformance: white-box structure + black-box behavior.
 * @run main BeanContextTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextSupport;

public class BeanContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static class Kid extends BeanContextSupport {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContext");
        check(c.isInterface(), "is an interface");
        check(BeanContext.globalHierarchyLock != null, "globalHierarchyLock non-null");
        java.util.Set<String> got = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) got.add(i.getName());
        check(got.contains("java.beans.DesignMode") && got.contains("java.beans.Visibility") &&
              got.contains("java.beans.beancontext.BeanContextChild") && got.contains("java.util.Collection"), "supertypes");
    }

    static void testBlackBox() throws Exception {
        BeanContext bc = new BeanContextSupport();
        Object o = bc.instantiateChild("java.lang.String");
        check(o instanceof String, "instantiateChild");

        final int[] n = new int[1];
        BeanContextMembershipListener l = new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { n[0]++; }
            public void childrenRemoved(BeanContextMembershipEvent e) {}
        };
        bc.addBeanContextMembershipListener(l);
        bc.removeBeanContextMembershipListener(l);
        check(n[0] == 0, "membership listener add/remove no fire");

        boolean npe = false;
        try { bc.getResourceAsStream(null, null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getResourceAsStream null args NPE");
        boolean iae = false;
        try { bc.getResource("x", null); } catch (NullPointerException e) { iae = true; }
        check(iae, "getResource null child NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
