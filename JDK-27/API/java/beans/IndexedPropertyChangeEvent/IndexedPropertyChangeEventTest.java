/*
 * @test
 * @summary java.beans.IndexedPropertyChangeEvent conformance: white-box
 *          structure + black-box behavior, deepened to full contract:
 *          constructor field-by-field, index boundaries (zero, MAX_VALUE,
 *          negative values accepted with no validation), inheritance
 *          (PropertyChangeEvent/EventObject/Serializable), inherited
 *          propagationId, serialization round-trip (index and all fields
 *          survive, transient source reads back null), toString format
 *          including the index field, and firing a directly constructed
 *          indexed event through PropertyChangeSupport.firePropertyChange
 *          (exact event identity, index preserved, global + named delivery,
 *          equal old/new not fired).
 * @run main IndexedPropertyChangeEventTest
 */

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class IndexedPropertyChangeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===== original coverage (kept verbatim) =====
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.IndexedPropertyChangeEvent");
        check(PropertyChangeEvent.class.isAssignableFrom(c), "extends PropertyChangeEvent");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent(src, "items", "a", "b", 3);
        check(e.getIndex() == 3, "index");
        check("items".equals(e.getPropertyName()), "property name inherited");
        check("a".equals(e.getOldValue()) && "b".equals(e.getNewValue()), "old/new inherited");
        check(e.getSource() == src, "source inherited");
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

    static void testConstructorContract() {
        Object src = new Object();
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent(src, "items", "old", "new", 3);
        check(src == e.getSource(), "source identity");
        check("items".equals(e.getPropertyName()), "property name");
        check("old".equals(e.getOldValue()) && "new".equals(e.getNewValue()), "old/new values");
        check(e.getIndex() == 3, "index value");
        check(e.getPropagationId() == null, "propagationId defaults to null (not a constructor field)");

        IndexedPropertyChangeEvent zero = new IndexedPropertyChangeEvent(src, "a", "o", "n", 0);
        check(zero.getIndex() == 0, "index zero accepted");
        IndexedPropertyChangeEvent max = new IndexedPropertyChangeEvent(src, "a", "o", "n", Integer.MAX_VALUE);
        check(max.getIndex() == Integer.MAX_VALUE, "Integer.MAX_VALUE index accepted");

        // boxed old/new values are plain Objects
        IndexedPropertyChangeEvent boxed =
                new IndexedPropertyChangeEvent(src, "a", Integer.valueOf(1), Integer.valueOf(2), 5);
        check(((Integer) boxed.getOldValue()).intValue() == 1
                && ((Integer) boxed.getNewValue()).intValue() == 2, "boxed old/new values accepted");
    }

    static void testNegativeIndex() {
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent("s", "p", "o", "n", -1);
        check(e.getIndex() == -1, "negative index accepted without validation");
        IndexedPropertyChangeEvent min = new IndexedPropertyChangeEvent("s", "p", "o", "n", Integer.MIN_VALUE);
        check(min.getIndex() == Integer.MIN_VALUE, "Integer.MIN_VALUE index accepted");
    }

    static void testInheritance() {
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent("s", "p", "o", "n", 1);
        check(e instanceof PropertyChangeEvent, "instanceof PropertyChangeEvent");
        check(e instanceof java.util.EventObject, "instanceof java.util.EventObject");
        check(e instanceof java.io.Serializable, "Serializable");
        e.setPropagationId("pid");
        check("pid".equals(e.getPropagationId()), "inherited propagationId set/get");
        check(e.getSource() instanceof String, "inherited getSource delegates to EventObject");
    }

    static void testSerializationRoundTrip() throws Exception {
        SerialSource src = new SerialSource(4);
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent(src, "items", "a", "b", 7);
        e.setPropagationId(Integer.valueOf(2));
        IndexedPropertyChangeEvent rt = (IndexedPropertyChangeEvent) roundtrip(e);
        check(rt instanceof IndexedPropertyChangeEvent, "round-trip preserves the concrete event class");
        check(rt.getIndex() == 7, "index survives serialization");
        check("items".equals(rt.getPropertyName()), "property name survives serialization");
        check("a".equals(rt.getOldValue()) && "b".equals(rt.getNewValue()),
                "old/new values survive serialization");
        check(Integer.valueOf(2).equals(rt.getPropagationId()), "propagationId survives serialization");
        check(rt.getSource() == null, "source is transient and reads back null after round-trip");
    }

    static void testToString() {
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent("src", "items", "o", "n", 12);
        String s = e.toString();
        check(s != null, "toString non-null");
        check(s.contains("index=12"), "toString contains the index field");
        check(s.contains("items"), "toString contains the property name");
        IndexedPropertyChangeEvent neg = new IndexedPropertyChangeEvent("src", "items", "o", "n", -5);
        check(neg.toString().contains("index=-5"), "toString renders a negative index");
        check(s.contains("java.beans.IndexedPropertyChangeEvent"), "toString starts with the class name");
    }

    static void testFireThroughSupport() {
        Object src = new Object();
        PropertyChangeSupport pcs = new PropertyChangeSupport(src);
        Recording global = new Recording();
        Recording named = new Recording();
        pcs.addPropertyChangeListener(global);
        pcs.addPropertyChangeListener("items", named);
        IndexedPropertyChangeEvent sent = new IndexedPropertyChangeEvent(src, "items", "a", "b", 4);
        pcs.firePropertyChange(sent);
        check(global.count == 1, "directly constructed indexed event fires to a global listener");
        check(global.last == sent, "the exact event object is delivered (identity preserved)");
        check(((IndexedPropertyChangeEvent) global.last).getIndex() == 4,
                "index preserved through firePropertyChange(event) delivery");
        check(named.count == 1, "named listener of that property also receives the indexed event");
        check(named.last == sent, "named listener receives the same event object");

        pcs.firePropertyChange(new IndexedPropertyChangeEvent(src, "items", "same", "same", 4));
        check(global.count == 1, "indexed event with equal non-null old/new is not fired");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorContract();
        testNegativeIndex();
        testInheritance();
        testSerializationRoundTrip();
        testToString();
        testFireThroughSupport();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
