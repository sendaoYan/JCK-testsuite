/*
 * @test
 * @summary java.beans.beancontext.BeanContextMembershipEvent conformance: white-box structure + black-box behavior.
 *          Both constructors (Object[] and Collection): source identity, exact children
 *          contents with identity preservation, size/toArray/iterator/contains for both
 *          ctor kinds, empty children, equals-based contains, negative matrix
 *          (null children -> NPE, null source -> IAE from EventObject), array-ctor
 *          iterator remove -> UOE vs Collection-ctor remove allowed, toArray defensive
 *          copies, propagation interplay inherited from BeanContextEvent, and
 *          serialization round-trip (transient source, surviving children and
 *          propagatedFrom).
 * @run main BeanContextMembershipEventTest
 */

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextEvent;
import java.beans.beancontext.BeanContextMembershipEvent;
import java.beans.beancontext.BeanContextSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class BeanContextMembershipEventTest {
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
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextMembershipEvent");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(java.beans.beancontext.BeanContextEvent.class.isAssignableFrom(c), "extends BeanContextEvent");
        check("java.beans.beancontext.BeanContextEvent".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextEvent");
        check(hasMethod(c, "contains"), "declares contains");
        check(hasMethod(c, "iterator"), "declares iterator");
        check(hasMethod(c, "size"), "declares size");
        check(hasMethod(c, "toArray"), "declares toArray");
        check(c.getDeclaredConstructors().length == 2, "two declared constructors");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 3499346510334590959L, "serialVersionUID stable");
    }

    static void testBlackBox() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        Object[] kids = {"a", "b"};
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(bc, kids);
        check(bc == e.getBeanContext(), "getBeanContext");
        check(e.getSource() == bc, "getSource identity");
        check(e.size() == 2, "size");
        check(e.contains("a") && e.contains("b"), "contains");
        check(Arrays.equals(new Object[]{"a","b"}, e.toArray()), "toArray");
        int n = 0; for (Iterator it = e.iterator(); it.hasNext(); it.next()) n++;
        check(n == 2, "iterator");

        BeanContextMembershipEvent e2 = new BeanContextMembershipEvent(bc, Arrays.asList(kids));
        check(e2.size() == 2, "Collection ctor size");
        check(e2.contains("a") && e2.contains("b") && e2.getBeanContext() == bc, "Collection ctor contents/source");
        check(e2.getSource() == bc, "Collection ctor getSource");
        // iterator yields the exact same instances (identity)
        Object o1 = new Object(), o2 = new Object();
        BeanContextMembershipEvent e3 = new BeanContextMembershipEvent(bc, new Object[]{o1, o2});
        boolean found1 = false, found2 = false;
        for (Iterator it = e3.iterator(); it.hasNext(); ) {
            Object x = it.next();
            if (x == o1) found1 = true;
            if (x == o2) found2 = true;
        }
        check(found1 && found2, "iterator preserves identity");
        // toArray identity
        Object[] arr = e3.toArray();
        check(arr.length == 2, "toArray length");
        boolean id1 = false, id2 = false;
        for (Object x : arr) { if (x == o1) id1 = true; if (x == o2) id2 = true; }
        check(id1 && id2, "toArray preserves identity");
    }

    static void testContentsSemantics() throws Exception {
        BeanContextSupport bc = new BeanContextSupport();
        // empty children
        BeanContextMembershipEvent empty = new BeanContextMembershipEvent(bc, new Object[0]);
        check(empty.size() == 0, "empty size 0");
        check(empty.toArray().length == 0, "empty toArray");
        check(!empty.iterator().hasNext(), "empty iterator");
        check(!empty.contains(new Object()), "empty contains false");
        // equals-based contains
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(bc, new Object[]{"a"});
        check(e.contains(new String("a")), "contains is equals-based");
        check(!e.contains("z"), "absent child not contained");
        check(!e.contains(null), "null child not contained");
        // toArray returns a fresh copy each call
        Object[] first = e.toArray();
        first[0] = "mutated";
        check("a".equals(e.toArray()[0]), "toArray defensive copy");
        // iterator order not asserted, but exhaustive iteration hits each element once
        BeanContextMembershipEvent multi = new BeanContextMembershipEvent(bc, new Object[]{"x", "y", "z"});
        Set<Object> seen = new HashSet<Object>();
        Iterator it = multi.iterator();
        while (it.hasNext()) seen.add(it.next());
        check(seen.equals(new HashSet<Object>(Arrays.asList("x", "y", "z"))), "iterator covers each child once");
        check(multi.size() == 3, "multi size");
        // next past the end
        final Iterator endIt = multi.iterator();
        while (endIt.hasNext()) endIt.next();
        checkThrows(NoSuchElementException.class, new ThrowingRunnable() {
            public void run() { endIt.next(); } }, "next past end -> NoSuchElementException");
        // hasNext idempotent
        Iterator<Boolean> it2 = multi.iterator();
        check(it2.hasNext() && it2.hasNext(), "hasNext idempotent");
        // Collection ctor with a Set
        BeanContextMembershipEvent setEv = new BeanContextMembershipEvent(bc, new HashSet<Object>(Arrays.asList("s1", "s2")));
        check(setEv.size() == 2 && setEv.contains("s1") && setEv.contains("s2"), "Set ctor contents");
        // Object[] ctor keeps a live view (Arrays.asList) of the passed array:
        // later mutation of the array is visible through the event
        Object[] passed = new Object[]{"p1", "p2"};
        BeanContextMembershipEvent mut = new BeanContextMembershipEvent(bc, passed);
        passed[0] = "changed";
        check(mut.contains("changed") && !mut.contains("p1"), "Object[] ctor wraps a live view, not a snapshot");
        check(mut.size() == 2, "live view keeps size");
    }

    static void testNegativeMatrix() throws Exception {
        final BeanContextSupport bc = new BeanContextSupport();
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new BeanContextMembershipEvent(bc, (Object[]) null); } },
            "Object[] ctor with null children -> NPE");
        checkThrows(NullPointerException.class, new ThrowingRunnable() {
            public void run() { new BeanContextMembershipEvent(bc, (java.util.Collection) null); } },
            "Collection ctor with null children -> NPE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextMembershipEvent(null, new Object[0]); } },
            "null source -> IAE (EventObject contract)");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new BeanContextMembershipEvent(null, new ArrayList<Object>()); } },
            "null source Collection ctor -> IAE");
        // array-ctor iterator remove is unsupported (Arrays.asList is fixed-size)
        final BeanContextMembershipEvent arrEv = new BeanContextMembershipEvent(bc, new Object[]{"r"});
        final Iterator arrIt = arrEv.iterator();
        arrIt.next();
        checkThrows(UnsupportedOperationException.class, new ThrowingRunnable() {
            public void run() { arrIt.remove(); } }, "array-ctor iterator remove -> UOE");
        // Collection-ctor with ArrayList: iterator remove allowed and reflected
        BeanContextMembershipEvent listEv = new BeanContextMembershipEvent(bc, new ArrayList<Object>(Arrays.asList("r")));
        Iterator listIt = listEv.iterator();
        listIt.next();
        listIt.remove();
        check(listEv.size() == 0, "Collection-ctor iterator remove reflected in size");
    }

    static void testPropagation() throws Exception {
        BeanContextSupport src = new BeanContextSupport();
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(src, new Object[]{"k"});
        check(e.getPropagatedFrom() == null, "propagatedFrom null default");
        check(!e.isPropagated(), "not propagated default");
        BeanContextSupport other = new BeanContextSupport();
        e.setPropagatedFrom(other);
        check(e.getPropagatedFrom() == other, "propagatedFrom set");
        check(e.isPropagated(), "isPropagated true after set");
        e.setPropagatedFrom(null);
        check(e.getPropagatedFrom() == null && !e.isPropagated(), "reset to null clears propagation");
        // typed as the base event class
        BeanContextEvent base = e;
        check(base.getBeanContext() == src, "base-type getBeanContext");
    }

    static void testSerialization() throws Exception {
        BeanContextSupport src = new BeanContextSupport();
        BeanContextMembershipEvent e = new BeanContextMembershipEvent(src, new Object[]{"k1", "k2"});
        e.setPropagatedFrom(src);
        BeanContextMembershipEvent back = deser(ser(e));
        check(back.getSource() == null, "EventObject source is transient: null after round-trip");
        check(back.getBeanContext() == null, "getBeanContext null after round-trip");
        check(back.size() == 2, "children survive round-trip");
        check(back.contains("k1") && back.contains("k2"), "children contents survive");
        check(back.getPropagatedFrom() != null, "propagatedFrom survives round-trip (serialized copy)");
        check(back.isPropagated(), "still propagated after round-trip");
        // the propagatedFrom copy is not the original source instance
        check(back.getPropagatedFrom() != src, "propagatedFrom is a deserialized copy");
        // without propagation set
        BeanContextMembershipEvent plain = deser(ser(new BeanContextMembershipEvent(src, new Object[]{"z"})));
        check(plain.getPropagatedFrom() == null && !plain.isPropagated(), "unpropagated stays unpropagated");
        check(plain.contains("z"), "contents survive without propagation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContentsSemantics();
        testNegativeMatrix();
        testPropagation();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
