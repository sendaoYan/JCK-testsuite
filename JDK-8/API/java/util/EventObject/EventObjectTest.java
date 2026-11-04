/*
 * @test
 * @summary java.util.EventObject API conformance: getSource identity, null-source rejection
 *          (IllegalArgumentException "null source"), toString format, identity equals,
 *          Serializable marker with transient source (getSource()==null after round-trip).
 * @run main EventObjectTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EventObject;

public class EventObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static class SubEvent extends EventObject {
        SubEvent(Object source) { super(source); }
    }

    static void testEventObject() {
        Object src = new Object();
        EventObject e = new EventObject(src);
        check(e.getSource() == src, "getSource returns the constructor argument");
    }

    // ============ contract deepening (appended) ============

    static void testNullSource() {
        try {
            new EventObject(null);
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            check("null source".equals(ex.getMessage()), "null source message exact");
        }
        expectThrows(IllegalArgumentException.class, () -> new EventObject(null),
            "EventObject(null) IllegalArgumentException");
    }

    static void testToStringFormat() {
        Object src = "SRC";
        EventObject e = new EventObject(src);
        // documented format: getClass().getName() + "[source=" + source + "]"
        check(("java.util.EventObject[source=" + src + "]").equals(e.toString()),
              "toString exact documented format");
        SubEvent sub = new SubEvent(src);
        check((SubEvent.class.getName() + "[source=" + src + "]").equals(sub.toString()),
              "subclass toString names its own class");
        check(e.toString().equals(e.toString()), "toString idempotent");
    }

    static void testSourceStability() {
        Object src = new Object();
        EventObject e = new EventObject(src);
        check(e.getSource() == e.getSource(), "getSource stable across calls");
        SubEvent sub = new SubEvent(src);
        check(sub.getSource() == src, "getSource works in subclass");
        // any non-null object is a valid source
        check(new EventObject(42).getSource().equals(42), "boxed primitive source");
        check(new EventObject(new int[0]).getSource() instanceof int[], "array source");
    }

    static void testIdentityEquality() {
        Object src = new Object();
        EventObject a = new EventObject(src);
        EventObject b = new EventObject(src);
        check(!a.equals(b), "same source does not imply equality (identity equals)");
        check(a.equals(a), "identity equals reflexive");
        check(!a.equals(null) && !a.equals("x"), "equals(null)/other type false");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
    }

    static void testSerializable() throws Exception {
        check(Serializable.class.isAssignableFrom(EventObject.class),
              "EventObject implements Serializable");
        check((Object) new EventObject("s") instanceof Serializable, "events are Serializable");
        EventObject eo = new EventObject("PERSIST");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(eo);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        EventObject back = (EventObject) ois.readObject();
        ois.close();
        // the source field is transient: reviving yields a null source (documented field modifier)
        check(back.getSource() == null, "source is transient: null after round-trip");
        check(!back.equals(eo), "identity equals: revived event differs from original");
        check(back != eo, "revived event is a new object");
        check(back.toString().contains("source=null"), "toString renders transient null source");
    }


    // ===================== 契约深化：null source IAE/Serializable/toString 前缀 =====================
    static void testEventObjectContracts() {
        Object src = new Object();
        EventObject e = new EventObject(src);
        check(e.getSource() == src, "getSource identity");
        check(e.toString().startsWith("java.util.EventObject"),
              "toString starts with class name");
        try { new EventObject(null); check(false, "EventObject(null) IAE"); }
        catch (IllegalArgumentException iae) { check(true, "EventObject(null) IAE"); }
        check(java.io.Serializable.class.isAssignableFrom(EventObject.class),
              "EventObject is Serializable");
    }

    public static void main(String[] args) throws Exception {
        testEventObject();
        testNullSource();
        testToStringFormat();
        testSourceStability();
        testIdentityEquality();
        testSerializable();
        testEventObjectContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
