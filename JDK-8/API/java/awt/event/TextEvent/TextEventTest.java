/*
 * @test
 * @summary java.awt.event.TextEvent conformance: white-box structure + black-box behavior.
 * @run main TextEventTest
 */

import java.awt.AWTEvent;
import java.awt.event.TextEvent;
import java.lang.reflect.Modifier;

public class TextEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.TextEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "TextEvent is public");
        check(!Modifier.isAbstract(m), "TextEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "TextEvent extends AWTEvent");
        check(c.getField("TEXT_VALUE_CHANGED").getType() == int.class, "TEXT_VALUE_CHANGED int");
        check(c.getField("TEXT_FIRST").getType() == int.class, "TEXT_FIRST int");
        check(c.getField("TEXT_LAST").getType() == int.class, "TEXT_LAST int");
        check(c.getConstructor(Object.class, int.class) != null, "has (Object,int) ctor");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        TextEvent e = new TextEvent(src, TextEvent.TEXT_VALUE_CHANGED);

        check(e.getID() == TextEvent.TEXT_VALUE_CHANGED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.paramString().equals("TEXT_VALUE_CHANGED"), "paramString TEXT_VALUE_CHANGED");

        TextEvent unk = new TextEvent(src, 9999);
        check(unk.paramString().equals("unknown type"), "paramString unknown type");

        // documented constants
        check(TextEvent.TEXT_FIRST == 900, "TEXT_FIRST == 900");
        check(TextEvent.TEXT_LAST == 900, "TEXT_LAST == 900");
        check(TextEvent.TEXT_VALUE_CHANGED == 900, "TEXT_VALUE_CHANGED == 900");

        // id range relationship
        check(TextEvent.TEXT_FIRST <= TextEvent.TEXT_VALUE_CHANGED && TextEvent.TEXT_VALUE_CHANGED <= TextEvent.TEXT_LAST, "id within range");

        check(e instanceof AWTEvent, "TextEvent instanceof AWTEvent");
        // consume semantics: consume()/isConsumed() are protected in AWTEvent, exercised via a local subclass
        // consume semantics: semantic event ids are pre-consumed by AWTEvent's constructor
        Consumable ev = new Consumable();
        check(ev.consumedFlag(), "TextEvent" + " pre-consumed at construction");
        ev.consumeIt();
        check(ev.consumedFlag(), "consume keeps consumed");
        // toString
        check(e.toString().startsWith("java.awt.event.TextEvent["), "toString starts with class name");
        check(e.toString().contains("TEXT_VALUE_CHANGED"), "toString contains id text");
    }

    static class Consumable extends TextEvent {
        Consumable() { super("src", TextEvent.TEXT_VALUE_CHANGED); }
        void consumeIt() { consume(); }
        boolean consumedFlag() { return isConsumed(); }
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        java.awt.event.TextEvent e = new java.awt.event.TextEvent("src", java.awt.event.TextEvent.TEXT_VALUE_CHANGED);
        java.awt.event.TextEvent back = roundTrip(e);
        check(back.getID() == java.awt.event.TextEvent.TEXT_VALUE_CHANGED, "serialization preserves id");
        check(back.paramString().equals("TEXT_VALUE_CHANGED"), "serialization keeps paramString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
