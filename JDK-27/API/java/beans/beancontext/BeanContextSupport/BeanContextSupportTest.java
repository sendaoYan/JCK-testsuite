/*
 * @test
 * @summary java.beans.beancontext.BeanContextSupport conformance: white-box structure + black-box behavior.
 *          Membership contract (add/remove/contains/size/isEmpty/containsAll), negative matrix
 *          (add(null)->IAE, remove(null)->IAE, null membership listener->NPE, resource
 *          null/non-member->NPE/IAE), toArray both variants, membership events with exact
 *          child identity, duplicate add/listener semantics, iterator contract,
 *          unsupported bulk ops -> UOE, locale/designTime local flags with exact property
 *          events, GUI visibility interplay, child protocol (setBeanContext veto ->
 *          IllegalStateException, child detach auto-removal, nested contexts),
 *          peer semantics, serialization round-trip, and reflection structure.
 * @run main BeanContextSupportTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextMembershipListener;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BeanContextSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    static class VChild implements java.beans.Visibility {
        boolean gui = true;
        public boolean needsGui() { return gui; }
        public void dontUseGui() { gui = false; }
        public void okToUseGui() { gui = true; }
        public boolean avoidingGui() { return !gui; }
    }

    static class ConstantGui implements java.beans.Visibility {
        public boolean needsGui() { return true; }
        public void dontUseGui() { }
        public void okToUseGui() { }
        public boolean avoidingGui() { return true; }
    }

    /** Records membership events including exact child identity. */
    static class Rec implements BeanContextMembershipListener {
        int added, removed;
        Object[] lastAdded, lastRemoved;
        Object srcAdded;
        public void childrenAdded(BeanContextMembershipEvent e) { added++; lastAdded = e.toArray(); srcAdded = e.getBeanContext(); }
        public void childrenRemoved(BeanContextMembershipEvent e) { removed++; lastRemoved = e.toArray(); }
    }

    static class VetoChild extends BeanContextChildSupport {
        VetoChild() {
            addVetoableChangeListener("beanContext", new VetoableChangeListener() {
                public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                    if (e.getNewValue() != null) throw new PropertyVetoException("veto", e);
                }
            });
        }
    }

    static class RejectAdd extends BeanContextSupport {
        protected boolean validatePendingAdd(Object targetChild) { return false; }
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }
    static <T> T deser(byte[] b) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
        @SuppressWarnings("unchecked") T t = (T) ois.readObject();
        ois.close();
        return t;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextSupport");
        check(java.beans.beancontext.BeanContext.class.isAssignableFrom(c), "implements BeanContext");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        // Collection surface comes via BeanContext extends Collection
        check(java.util.Collection.class.isAssignableFrom(c), "implements Collection via BeanContext");
        check(java.beans.PropertyChangeListener.class.isAssignableFrom(c), "implements PropertyChangeListener");
        check(java.beans.VetoableChangeListener.class.isAssignableFrom(c), "implements VetoableChangeListener");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "addBeanContextMembershipListener"), "declares addBeanContextMembershipListener");
        // exactly two nested classes: BCSChild and BCSIterator (order not guaranteed, sort first)
        List<String> nested = new ArrayList<String>();
        for (Class<?> n : c.getDeclaredClasses()) nested.add(n.getSimpleName());
        java.util.Collections.sort(nested);
        check(nested.equals(Arrays.asList("BCSChild", "BCSIterator")), "nested classes exactly BCSChild,BCSIterator: " + nested);
        // stable serialVersionUID (JDK 1.3 FCS value)
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == -4879613978649577204L, "serialVersionUID stable");
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

    static void testMembershipContract() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        check(bc.isEmpty(), "empty initially");
        check(bc.size() == 0, "size 0 initially");
        check(bc.add("a"), "add returns true");
        check(bc.size() == 1, "size 1 after add");
        check(bc.contains("a"), "contains");
        check(bc.containsKey("a"), "containsKey");
        check(!bc.isEmpty(), "not empty after add");
        Rec rec = new Rec();
        bc.addBeanContextMembershipListener(rec);
        check(!bc.add("a"), "duplicate add returns false");
        check(bc.size() == 1, "duplicate add does not double size");
        check(rec.added == 0, "duplicate add fires no membership event");
        check(bc.remove("a"), "remove returns true");
        check(!bc.remove("a"), "remove absent returns false");
        check(bc.isEmpty() && bc.size() == 0, "empty after remove");
        check(!bc.contains("a") && !bc.containsKey("a"), "not contained after remove");
        bc.add("x"); bc.add("y");
        check(bc.containsAll(Arrays.asList("x")), "containsAll subset");
        check(bc.containsAll(Arrays.asList("x", "y")), "containsAll all");
        check(!bc.containsAll(Arrays.asList("x", "z")), "containsAll superset false");
        check(bc.containsAll(new ArrayList<Object>()), "containsAll empty true");
    }

    static void testNegativeMatrix() throws Exception {
        final BeanContextSupport bc = new BeanContextSupport();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bc.add(null); } }, "add(null) -> IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bc.remove(null); } }, "remove(null) -> IAE");
        check(!bc.contains(null), "contains(null) false");
        check(!bc.containsKey(null), "containsKey(null) false");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.addBeanContextMembershipListener(null); } }, "addBeanContextMembershipListener(null) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.removeBeanContextMembershipListener(null); } }, "removeBeanContextMembershipListener(null) -> NPE");
        // resource delegation
        final BeanContextChild member = new BeanContextChildSupport();
        bc.add(member);
        final BeanContextChild nonMember = new BeanContextChildSupport();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResourceAsStream(null, member); } }, "getResourceAsStream(null name) -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResourceAsStream("x", null); } }, "getResourceAsStream(null child) -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bc.getResourceAsStream("x", nonMember); } }, "getResourceAsStream(non-member) -> IAE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { bc.getResource(null, member); } }, "getResource(null name) -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { bc.getResource("x", nonMember); } }, "getResource(non-member) -> IAE");
        check(bc.getResourceAsStream("java/lang/String.class", member) != null, "member getResourceAsStream finds JDK resource");
        check(bc.getResource("java/lang/String.class", member) != null, "member getResource finds JDK resource URL");
    }

    static void testToArrayContract() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        bc.add("m"); bc.add("n");
        Object[] a = bc.toArray();
        Arrays.sort(a);
        check(Arrays.equals(new Object[]{"m", "n"}, a), "toArray() exact elements");
        check(bc.toArray(new Object[0]).length == 2, "toArray(Object[0]) length 2");
        String[] exact = (String[]) bc.toArray(new String[2]);
        check(exact.length == 2, "toArray(T[]) exact size length");
        Arrays.sort(exact);
        check("m".equals(exact[0]) && "n".equals(exact[1]), "toArray(T[]) exact contents");
        String[] big = new String[3];
        String[] res = (String[]) bc.toArray(big);
        check(res == big, "toArray(larger) reuses given array");
        check(java.util.Arrays.asList(res).contains("m") && java.util.Arrays.asList(res).contains("n"),
              "toArray(larger) contents");
        check(res[2] == null, "toArray(larger) null-terminated");
    }

    static void testMembershipEventDetails() throws Exception {
        final BeanContextSupport bc = new BeanContextSupport();
        Rec rec = new Rec();
        bc.addBeanContextMembershipListener(rec);
        Object kid1 = new Object();
        Object kid2 = new Object();
        bc.add(kid1);
        check(rec.added == 1, "childrenAdded fired once");
        check(rec.lastAdded.length == 1 && rec.lastAdded[0] == kid1, "childrenAdded carries exact child identity");
        check(rec.srcAdded == bc, "childrenAdded source is the context");
        bc.remove(kid1);
        check(rec.removed == 1 && rec.lastRemoved.length == 1 && rec.lastRemoved[0] == kid1,
              "childrenRemoved carries exact child identity");
        bc.add(kid2); bc.remove(kid2);
        check(rec.added == 2 && rec.removed == 2, "events fire per operation");
        // duplicate listener registration is idempotent
        bc.addBeanContextMembershipListener(rec);
        bc.add("dup");
        check(rec.added == 3, "duplicate registered listener still fires once");
        bc.removeBeanContextMembershipListener(rec);
        bc.removeBeanContextMembershipListener(rec); // repeated remove no-op
        bc.add("quiet");
        check(rec.added == 3, "removed listener no longer fires");
        bc.removeBeanContextMembershipListener(new Rec()); // never-added remove no-op
        check(true, "removing never-added listener no-op");
        // two independent listeners both notified
        final int[] counts = new int[2];
        Rec r1 = new Rec() { public void childrenAdded(BeanContextMembershipEvent e) { counts[0]++; } };
        Rec r2 = new Rec() { public void childrenAdded(BeanContextMembershipEvent e) { counts[1]++; } };
        bc.addBeanContextMembershipListener(r1);
        bc.addBeanContextMembershipListener(r2);
        bc.add("both");
        check(counts[0] == 1 && counts[1] == 1, "both listeners notified");
        // listener RuntimeException propagates after the child was added
        final BeanContextSupport bct = new BeanContextSupport();
        final Rec later = new Rec();
        bct.addBeanContextMembershipListener(new BeanContextMembershipListener() {
            public void childrenAdded(BeanContextMembershipEvent e) { throw new RuntimeException("boom"); }
            public void childrenRemoved(BeanContextMembershipEvent e) { }
        });
        bct.addBeanContextMembershipListener(later);
        try {
            bct.add("boom");
            check(false, "throwing listener must propagate");
        } catch (RuntimeException re) {
            check("boom".equals(re.getMessage()), "listener RuntimeException propagates from add");
        }
        check(bct.size() == 1, "child added despite listener exception");
        check(later.added == 0, "listeners after the throwing one are skipped");
    }

    static void testIteratorContract() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        bc.add("i1"); bc.add("i2"); bc.add("i3");
        Set<Object> seen = new HashSet<Object>();
        Iterator<Object> it = bc.iterator();
        while (it.hasNext()) seen.add(it.next());
        check(seen.equals(new HashSet<Object>(Arrays.asList("i1", "i2", "i3"))), "iterator yields exactly the children");
        check(bc.size() == 3, "iteration does not modify context");
        Iterator<Object> it2 = bc.iterator();
        check(it2 != it && it2.hasNext(), "fresh iterator each call");
        check(it.hasNext() == it.hasNext(), "hasNext idempotent");
        BeanContextSupport empty = new BeanContextSupport();
        check(!empty.iterator().hasNext(), "empty context iterator hasNext false");
    }

    static void testUnsupportedBulkOps() throws Exception {
        final BeanContextSupport bc = new BeanContextSupport();
        bc.add("u");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { bc.addAll(Arrays.asList("v")); } }, "addAll -> UOE");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { bc.removeAll(Arrays.asList("u")); } }, "removeAll -> UOE");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { bc.retainAll(Arrays.asList("u")); } }, "retainAll -> UOE");
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { bc.clear(); } }, "clear -> UOE");
        check(bc.size() == 1, "children unchanged after UOE attempts");
    }

    static void testLocaleAndDesignTime() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        check(!bc.isDesignTime(), "default designTime false");
        check(bc.getLocale() != null && Locale.getDefault().equals(bc.getLocale()), "default locale is Locale.getDefault()");
        final List<PropertyChangeEvent> evs = new ArrayList<PropertyChangeEvent>();
        bc.addPropertyChangeListener("designMode", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evs.add(e); }
        });
        bc.setDesignTime(true);
        check(evs.size() == 1, "setDesignTime fires designMode propertyChange");
        check("designMode".equals(evs.get(0).getPropertyName()), "designMode event name");
        check(Boolean.FALSE.equals(evs.get(0).getOldValue()) && Boolean.TRUE.equals(evs.get(0).getNewValue()),
              "designMode event old/new exact");
        check(bc.isDesignTime(), "isDesignTime true after set");
        evs.clear();
        bc.setDesignTime(true);
        check(evs.isEmpty(), "setDesignTime same value fires nothing");
        bc.setDesignTime(false);
        check(!bc.isDesignTime(), "designTime back to false");
        // locale
        final List<PropertyChangeEvent> locEvs = new ArrayList<PropertyChangeEvent>();
        bc.addPropertyChangeListener("locale", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { locEvs.add(e); }
        });
        // 默认 locale 来自 Locale.getDefault()：Linux CI 上就是 en_US，所以这次
        // setLocale(Locale.US) 是“同值”；Windows 中文环境默认是 zh_CN，同一次调用
        // 会是真实变更并正当地触发一次事件。先清空已记录事件，再验证同值不触发。
        bc.setLocale(Locale.US);
        check(Locale.US.equals(bc.getLocale()), "setLocale takes effect");
        locEvs.clear();
        bc.setLocale(Locale.US);
        check(locEvs.isEmpty(), "setLocale same value fires nothing");
        bc.setLocale(null);
        check(Locale.US.equals(bc.getLocale()) && locEvs.isEmpty(), "setLocale(null) is a no-op");
        bc.addVetoableChangeListener("locale", new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {
                if (Locale.FRANCE.equals(e.getNewValue())) throw new PropertyVetoException("no", e);
            }
        });
        try {
            bc.setLocale(Locale.FRANCE);
            check(false, "locale veto must throw");
        } catch (PropertyVetoException pve) {
            check(true, "setLocale veto throws PropertyVetoException");
        }
        check(Locale.US.equals(bc.getLocale()), "locale unchanged after veto");
        bc.setLocale(Locale.CANADA);
        check(locEvs.size() == 1, "setLocale different value fires locale propertyChange");
        check(Locale.US.equals(locEvs.get(0).getOldValue()) && Locale.CANADA.equals(locEvs.get(0).getNewValue()),
              "locale event old/new exact");
        check(Locale.CANADA.equals(bc.getLocale()), "locale changed without veto");
        // ctors carrying locale/designTime
        BeanContextSupport bc2 = new BeanContextSupport(null, Locale.US, true);
        check(bc2.isDesignTime(), "3-arg ctor designTime true");
        check(Locale.US.equals(bc2.getLocale()), "3-arg ctor locale");
        BeanContextSupport bc3 = new BeanContextSupport(null, Locale.ITALY);
        check(Locale.ITALY.equals(bc3.getLocale()) && !bc3.isDesignTime(), "2-arg ctor locale + runtime");
        BeanContextSupport bc4 = new BeanContextSupport(null, null, false, false);
        check(Locale.getDefault().equals(bc4.getLocale()), "null locale falls back to default");
    }

    static void testGuiVisibility() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        check(!bc.needsGui(), "needsGui false empty");
        check(!bc.avoidingGui(), "avoidingGui false empty (okToUseGui default true)");
        VChild v = new VChild();
        bc.add(v);
        check(bc.needsGui(), "needsGui true with Visibility child needing gui");
        check(!bc.avoidingGui(), "avoidingGui false while gui allowed");
        bc.dontUseGui();
        check(!v.gui && v.avoidingGui(), "dontUseGui propagates to Visibility child");
        check(!bc.needsGui(), "needsGui false after dontUseGui");
        check(!bc.avoidingGui(), "avoidingGui false: !okToUseGui but needsGui false");
        bc.okToUseGui();
        check(v.gui, "okToUseGui propagates to child");
        check(bc.needsGui() && !bc.avoidingGui(), "needsGui true / avoidingGui false after okToUseGui");
        // Visibility child whose needsGui() ignores dontUseGui
        BeanContextSupport bc2 = new BeanContextSupport();
        bc2.add(new ConstantGui());
        check(bc2.needsGui(), "constant child keeps needsGui true");
        check(!bc2.avoidingGui(), "avoidingGui false while gui allowed");
        bc2.dontUseGui();
        check(bc2.needsGui(), "needsGui still true after dontUseGui (constant child)");
        check(bc2.avoidingGui(), "avoidingGui true: !okToUseGui && needsGui");
        bc2.okToUseGui();
        check(!bc2.avoidingGui(), "avoidingGui false after okToUseGui");
        // plain non-Visibility child never implies GUI
        BeanContextSupport bc3 = new BeanContextSupport();
        bc3.add("plain");
        check(!bc3.needsGui(), "plain child does not imply gui");
    }

    static void testChildProtocol() throws Exception {
        // adding a BeanContextChild wires its beanContext property
        BeanContextSupport bc = new BeanContextSupport();
        BeanContextChildSupport kid = new BeanContextChildSupport();
        bc.add(kid);
        check(kid.getBeanContext() == bc, "added child's getBeanContext is the context");
        // removing notifies the child
        final Object[] got = new Object[2];
        kid.addPropertyChangeListener("beanContext", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { got[0] = e.getOldValue(); got[1] = e.getNewValue(); }
        });
        Rec rec = new Rec();
        bc.addBeanContextMembershipListener(rec);
        bc.remove(kid);
        check(got[0] == bc && got[1] == null, "remove notifies child old=ctx new=null");
        check(rec.removed == 1, "remove fires childrenRemoved");
        // child detaching itself is honored: auto-removed from the context
        bc.add(kid);
        kid.setBeanContext(null);
        check(!bc.contains(kid) && bc.size() == 0, "child setBeanContext(null) auto-removes from context");
        check(rec.removed == 2 && rec.lastRemoved.length == 1 && rec.lastRemoved[0] == kid,
              "auto-removal fires childrenRemoved with the child");
        check(kid.getBeanContext() == null, "child detached");
        // vetoing child: add fails with IllegalStateException, not nested, no event
        BeanContextSupport bcv = new BeanContextSupport();
        Rec recv = new Rec();
        bcv.addBeanContextMembershipListener(recv);
        try {
            bcv.add(new VetoChild());
            check(false, "vetoing child add must throw");
        } catch (IllegalStateException ise) {
            check(true, "vetoing child add -> IllegalStateException");
        }
        check(bcv.size() == 0, "vetoing child not added");
        check(recv.added == 0, "no childrenAdded when child vetoes");
        // validatePendingAdd override returning false
        BeanContextSupport bcr = new RejectAdd();
        try {
            bcr.add("nope");
            check(false, "validatePendingAdd false must throw");
        } catch (IllegalStateException ise) {
            check(true, "validatePendingAdd false -> IllegalStateException");
        }
        check(bcr.size() == 0, "rejected child not added");
        // nested BeanContextSupport: a context is itself a BeanContextChild
        BeanContextSupport outer = new BeanContextSupport();
        BeanContextSupport inner = new BeanContextSupport();
        outer.add(inner);
        check(inner.getBeanContext() == outer, "nested context child link");
        check(outer.size() == 1 && outer.contains(inner), "nested context is a member");
    }

    static void testPeerSemantics() throws Exception {
        BeanContextSupport peer = new BeanContextSupport();
        BeanContextSupport bc = new BeanContextSupport(peer, Locale.US);
        check(bc.getBeanContextPeer() == peer, "peer ctor peer identity");
        check(Locale.US.equals(bc.getLocale()), "peer ctor locale");
        check(bc != peer && bc.getBeanContextPeer() != bc, "delegated peer is not self");
        Rec rec = new Rec();
        bc.addBeanContextMembershipListener(rec);
        bc.add("pk");
        check(rec.added == 1, "peer-backed context still fires events");
        check(rec.srcAdded == peer, "membership event source is the peer");
        // needsGui delegates to a Visibility peer
        BeanContextSupport peerV = new BeanContextSupport() {
            public boolean needsGui() { return true; }
        };
        BeanContextSupport bcg = new BeanContextSupport(peerV);
        check(bcg.needsGui(), "needsGui delegates to Visibility peer");
        // default: self peer
        check(new BeanContextSupport().getBeanContextPeer().equals(new BeanContextSupport()) == false
              && new BeanContextSupport().getBeanContextPeer() != null, "default ctor self peer non-null");
    }

    static void testSerializationContract() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        bc.setDesignTime(true);
        bc.add("s1"); bc.add("s2"); bc.add(new Object()); // Object is not Serializable
        BeanContextSupport back = deser(ser(bc));
        check(back.size() == 2, "non-serializable children silently dropped");
        check(back.contains("s1") && back.contains("s2"), "serializable children survive round-trip");
        check(back.isDesignTime(), "designTime survives round-trip");
        check(back.getLocale() != null, "locale survives round-trip");
        check(back.getBeanContextPeer() == back, "peer self after round-trip");
        check(!back.isSerializing(), "isSerializing false after round-trip");
        // attached serializable child is copied and re-nested
        BeanContextSupport bc2 = new BeanContextSupport();
        BeanContextChildSupport kid = new BeanContextChildSupport();
        bc2.add(kid);
        BeanContextSupport back2 = deser(ser(bc2));
        check(back2.size() == 1, "attached child survives");
        BeanContextChildSupport kidBack = (BeanContextChildSupport) back2.iterator().next();
        check(kidBack != kid, "deserialized child is a copy");
        check(kidBack.getBeanContext() != null, "deserialized child re-nested (beanContext restored)");
        // membership listeners are transient
        BeanContextSupport bc3 = new BeanContextSupport();
        Rec rec = new Rec();
        bc3.addBeanContextMembershipListener(rec);
        bc3.add("pre");
        BeanContextSupport back3 = deser(ser(bc3));
        back3.add("post");
        check(rec.added == 1, "membership listeners do not survive round-trip (transient)");
        check(back3.size() == 2, "children survive while listeners dropped");
        // non-serializable peer breaks writing the delegated context
        final BeanContextSupport badPeer = new BeanContextSupport() {
            private void writeObject(ObjectOutputStream o) throws IOException { throw new NotSerializableException("peer"); }
        };
        BeanContextSupport withPeer = new BeanContextSupport(badPeer);
        try {
            ser(withPeer);
            check(false, "non-serializable peer must fail");
        } catch (NotSerializableException nse) {
            check(true, "non-serializable peer -> NotSerializableException");
        }
        // serializable peer: link survives
        BeanContextSupport serPeer = new BeanContextSupport();
        BeanContextSupport w2 = new BeanContextSupport(serPeer);
        BeanContextChildSupport w2back = deser(ser(w2));
        check(w2back.getBeanContextChildPeer() instanceof BeanContextSupport, "peer survives round-trip");
        check(w2back.isDelegated(), "delegation survives round-trip");
    }

    static void testInstantiateChild() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        Rec rec = new Rec();
        bc.addBeanContextMembershipListener(rec);
        Object made = bc.instantiateChild("java.lang.String");
        check(made instanceof String, "instantiateChild creates the named class");
        check(bc.size() == 1 && bc.contains(made), "instantiateChild nests the new bean");
        check(rec.added == 1, "instantiateChild fires childrenAdded");
        try {
            bc.instantiateChild("no.such.BeanXYZ");
            check(false, "missing class must throw");
        } catch (ClassNotFoundException cnfe) {
            check(true, "instantiateChild missing class -> ClassNotFoundException");
        }
        try {
            bc.instantiateChild(null);
            check(false, "null beanName must throw");
        } catch (NullPointerException npe) {
            check(true, "instantiateChild(null) -> NullPointerException");
        }
    }

    static void testChangeHandlerHooks() throws Exception {
        // BeanContextSupport implements PropertyChangeListener/VetoableChangeListener;
        // events about unrelated sources/properties must not disturb it
        BeanContextSupport bc = new BeanContextSupport();
        bc.add("stable");
        bc.vetoableChange(new PropertyChangeEvent(new Object(), "other", "a", "b"));
        bc.propertyChange(new PropertyChangeEvent(new Object(), "beanContext", "a", "b"));
        check(bc.size() == 1, "unrelated propertyChange leaves children intact");
        bc.vetoableChange(new PropertyChangeEvent("stable", "beanContext", bc, null));
        check(bc.size() == 1, "vetoableChange without pending removal does not drop child");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMembershipContract();
        testNegativeMatrix();
        testToArrayContract();
        testMembershipEventDetails();
        testIteratorContract();
        testUnsupportedBulkOps();
        testLocaleAndDesignTime();
        testGuiVisibility();
        testChildProtocol();
        testPeerSemantics();
        testSerializationContract();
        testInstantiateChild();
        testChangeHandlerHooks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
