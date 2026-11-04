/*
 * @test
 * @key headful
 * @summary java.beans.beancontext.BeanContextSupport conformance: white-box structure + black-box behavior.
 * @run main BeanContextSupportTest
 */

import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextSupport;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

public class BeanContextSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class VChild implements java.beans.Visibility {
        boolean gui = true;
        public boolean needsGui() { return gui; }
        public void dontUseGui() { gui = false; }
        public void okToUseGui() { gui = true; }
        public boolean avoidingGui() { return !gui; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport");
        check(java.beans.beancontext.BeanContext.class.isAssignableFrom(c), "implements BeanContext");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testBlackBox() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        check(bc.getBeanContextPeer() == bc, "peer is self");
        check(bc.isEmpty() && bc.size() == 0, "empty initially");
        check(!bc.isSerializing(), "isSerializing false");

        check(bc.add("a"), "add");
        check(bc.size() == 1 && bc.contains("a"), "size/contains");
        check(bc.containsKey("a"), "containsKey");
        Iterator it = bc.iterator();
        check(it.hasNext() && "a".equals(it.next()), "iterator");
        check(bc.toArray().length == 1, "toArray");
        check(bc.toArray(new Object[0]).length == 1, "toArray(T[])");
        check(bc.containsAll(Arrays.asList("a")), "containsAll");
        boolean uoe = false;
        try { bc.addAll(Arrays.asList("b")); } catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "addAll throws UnsupportedOperationException");
        uoe = false;
        try { bc.removeAll(Arrays.asList("a")); } catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "removeAll throws UnsupportedOperationException");
        uoe = false;
        try { bc.retainAll(Arrays.asList("a")); } catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "retainAll throws UnsupportedOperationException");
        uoe = false;
        try { bc.clear(); } catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "clear throws UnsupportedOperationException");
        check(bc.remove("a"), "remove");
        check(bc.isEmpty(), "empty after remove");

        bc.setDesignTime(true); check(bc.isDesignTime(), "setDesignTime true");
        bc.setDesignTime(false); check(!bc.isDesignTime(), "setDesignTime false");
        bc.setLocale(Locale.US); check(Locale.US.equals(bc.getLocale()), "setLocale/getLocale");

        check(!bc.needsGui(), "needsGui false for empty context");
        check(!bc.avoidingGui(), "avoidingGui false for empty context");
        VChild v = new VChild();
        bc.add(v);
        check(bc.needsGui(), "needsGui true with Visibility child needing gui");
        bc.dontUseGui();
        check(v.avoidingGui(), "dontUseGui propagates to child");
        check(!bc.needsGui(), "needsGui false after dontUseGui");
        bc.okToUseGui();
        check(bc.needsGui(), "needsGui true after okToUseGui");

        Object o = bc.instantiateChild("java.lang.String");
        check(o instanceof String, "instantiateChild");

        final int[] added = new int[1], removed = new int[1];
        bc.addBeanContextMembershipListener(new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { added[0]++; }
            public void childrenRemoved(BeanContextMembershipEvent e) { removed[0]++; }
        });
        bc.add("kid");
        check(added[0] == 1, "childrenAdded fired on add");
        bc.remove("kid");
        check(removed[0] == 1, "childrenRemoved fired on remove");

        BeanContextSupport bc2 = new BeanContextSupport(null, Locale.US, true);
        check(bc2.isDesignTime(), "designTime ctor");
        check(Locale.US.equals(bc2.getLocale()), "locale ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
