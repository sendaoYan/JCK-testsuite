/*
 * @test
 * @summary java.beans.PropertyChangeEvent conformance: full contract.
 *          constructor + null source -> IllegalArgumentException, getters for
 *          source/propertyName/oldValue/newValue, propagationId default null
 *          and set/get round-trip (incl. null), and toString format containing
 *          property name, old/new values, propagation id and source.
 *          Deepening: boxed old/new values, identity equals/hashCode (no
 *          override), not Cloneable and no public clone, serialization
 *          round-trip (propertyName/oldValue/newValue/propagationId survive,
 *          source is transient and reads back null, non-serializable value ->
 *          NotSerializableException), delivery of a null-name event through
 *          PropertyChangeSupport, toString with null fields, and inheritance
 *          (EventObject/Serializable).
 * @run main PropertyChangeEventTest
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class PropertyChangeEventTest {
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

    static void testConstructor() {
        Object src = new Object();
        PropertyChangeEvent e = new PropertyChangeEvent(src, "name", "old", "new");
        check(src == e.getSource(), "source identity");
        check("name".equals(e.getPropertyName()), "propertyName");
        check("old".equals(e.getOldValue()), "oldValue");
        check("new".equals(e.getNewValue()), "newValue");
        check(e.getPropagationId() == null, "propagationId default null");

        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() {
                    new PropertyChangeEvent(null, "p", "a", "b");
                }}, "null source -> IllegalArgumentException");

        PropertyChangeEvent nulls = new PropertyChangeEvent(src, null, null, null);
        check(nulls.getPropertyName() == null && nulls.getOldValue() == null && nulls.getNewValue() == null,
                "null name/old/new accepted");
    }

    static void testPropagationId() {
        PropertyChangeEvent e = new PropertyChangeEvent(new Object(), "p", null, null);
        e.setPropagationId(Integer.valueOf(5));
        check(Integer.valueOf(5).equals(e.getPropagationId()), "propagationId set/get");
        e.setPropagationId(null);
        check(e.getPropagationId() == null, "propagationId set null");
    }

    static void testToString() {
        PropertyChangeEvent e = new PropertyChangeEvent("src", "p", "old", "new");
        e.setPropagationId("id");
        String s = e.toString();
        check(s != null && s.contains("p"), "toString contains property name");
        check(s.contains("old") && s.contains("new"), "toString contains old/new");
        check(s.contains("id"), "toString contains propagationId");
        check(s.contains("src"), "toString contains source");
    }


    static void testStructure() {
        check(java.util.EventObject.class.isAssignableFrom(PropertyChangeEvent.class), "extends EventObject");
    }

    // ===== appended: contract deepening (round A) =====

    /** Serializable source bean with value equality. */
    public static class SerialSource implements Serializable {
        private static final long serialVersionUID = 1L;
        int v;
        SerialSource(int v) { this.v = v; }
        public boolean equals(Object o) { return o instanceof SerialSource && ((SerialSource) o).v == v; }
        public int hashCode() { return v; }
    }

    static class Recording implements PropertyChangeListener {
        int count;
        PropertyChangeEvent last;
        public void propertyChange(PropertyChangeEvent e) { count++; last = e; }
    }

    static Object roundtrip(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Object r = ois.readObject();
        ois.close();
        return r;
    }

    static void testBoxedValues() {
        Object src = new Object();
        PropertyChangeEvent i = new PropertyChangeEvent(src, "i", Integer.valueOf(1), Integer.valueOf(2));
        check(i.getOldValue() instanceof Integer && i.getNewValue() instanceof Integer,
                "Integer old/new values boxed as Integer");
        check(((Integer) i.getOldValue()).intValue() == 1 && ((Integer) i.getNewValue()).intValue() == 2,
                "Integer old/new values preserved");
        PropertyChangeEvent b = new PropertyChangeEvent(src, "b", Boolean.TRUE, Boolean.FALSE);
        check(b.getOldValue() instanceof Boolean && b.getNewValue() instanceof Boolean,
                "Boolean old/new values boxed as Boolean");
        check(((Boolean) b.getOldValue()).booleanValue() && !((Boolean) b.getNewValue()).booleanValue(),
                "Boolean old/new values preserved");
        PropertyChangeEvent l = new PropertyChangeEvent(src, "l", Long.valueOf(5L), Long.valueOf(6L));
        check(((Long) l.getOldValue()).longValue() == 5L && ((Long) l.getNewValue()).longValue() == 6L,
                "Long old/new values preserved");
    }

    static void testEqualsHashCode() {
        Object src = new Object();
        PropertyChangeEvent a = new PropertyChangeEvent(src, "p", "o", "n");
        PropertyChangeEvent b = new PropertyChangeEvent(src, "p", "o", "n");
        check(a.equals(a), "equals is reflexive (identity semantics, no override)");
        check(!a.equals(b), "field-identical events are not equal (Object.equals identity semantics)");
        check(a.hashCode() == a.hashCode(), "hashCode stable within an instance");
        check(a.toString().equals(a.toString()), "toString stable within an instance");
    }

    static void testCloneNotSupported() {
        check(!java.lang.Cloneable.class.isAssignableFrom(PropertyChangeEvent.class),
                "PropertyChangeEvent does not implement Cloneable");
        try {
            PropertyChangeEvent.class.getMethod("clone");
            check(false, "PropertyChangeEvent exposes no public clone method");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        SerialSource src = new SerialSource(3);
        PropertyChangeEvent e = new PropertyChangeEvent(src, "prop", "old", "new");
        e.setPropagationId(Integer.valueOf(9));
        PropertyChangeEvent rt = (PropertyChangeEvent) roundtrip(e);
        check("prop".equals(rt.getPropertyName()), "propertyName survives serialization");
        check("old".equals(rt.getOldValue()) && "new".equals(rt.getNewValue()),
                "old/new values survive serialization");
        check(Integer.valueOf(9).equals(rt.getPropagationId()), "propagationId survives serialization");
        check(rt.getSource() == null, "source is transient and reads back as null after round-trip");

        checkThrows(NotSerializableException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    roundtrip(new PropertyChangeEvent(new SerialSource(1), "p", new Object(), "n"));
                }}, "non-serializable old value -> NotSerializableException");
    }

    static void testNullNameDelivery() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Recording r = new Recording();
        pcs.addPropertyChangeListener(r);
        pcs.firePropertyChange(null, "a", "b");
        check(r.count == 1, "an event with null property name is deliverable through the support");
        check(r.last.getPropertyName() == null, "delivered event keeps the null property name");
        check(src == r.last.getSource(), "source identity preserved on delivery");
        check("a".equals(r.last.getOldValue()) && "b".equals(r.last.getNewValue()),
                "old/new values delivered intact");
    }

    static void testToStringNulls() {
        PropertyChangeEvent e = new PropertyChangeEvent("s", null, null, null);
        String s = e.toString();
        check(s != null, "toString non-null with all-null fields");
        check(s.contains("null"), "toString renders null field values");
        check(s.startsWith("java.beans.PropertyChangeEvent"), "toString starts with the class name");
    }

    static void testInheritance() {
        PropertyChangeEvent e = new PropertyChangeEvent("s", "p", "o", "n");
        check(e instanceof java.util.EventObject, "instanceof java.util.EventObject");
        check(e instanceof java.io.Serializable, "Serializable via EventObject");
        check(e instanceof PropertyChangeEvent, "instanceof PropertyChangeEvent");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testConstructor();
        testPropagationId();
        testToString();
        testBoxedValues();
        testEqualsHashCode();
        testCloneNotSupported();
        testSerializationRoundTrip();
        testNullNameDelivery();
        testToStringNulls();
        testInheritance();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
