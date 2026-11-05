/*
 * @test
 * @summary javax.sound.midi.MidiEvent conformance: white-box structure + black-box behavior.
 * @run main MidiEventTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

public class MidiEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("getMessage"), "method getMessage");
        check(dm.contains("setTick"), "method setTick");
        check(dm.contains("getTick"), "method getTick");
        // message field is final; tick field is mutable.
        boolean messageFinal = false;
        boolean tickMutable = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("message")) messageFinal = Modifier.isFinal(f.getModifiers());
            if (f.getName().equals("tick")) tickMutable = !Modifier.isFinal(f.getModifiers());
        }
        check(messageFinal, "message field final");
        check(tickMutable, "tick field mutable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);

        MidiEvent ev = new MidiEvent(sm, 10L);
        check(ev.getMessage() == sm, "getMessage returns same message instance");
        check(ev.getTick() == 10L, "getTick initial");

        ev.setTick(20L);
        check(ev.getTick() == 20L, "setTick round-trip");

        ev.setTick(0L);
        check(ev.getTick() == 0L, "setTick zero");
        ev.setTick(-5L);
        check(ev.getTick() == -5L, "setTick negative");
        ev.setTick(Long.MAX_VALUE);
        check(ev.getTick() == Long.MAX_VALUE, "setTick max");

        MetaMessage mm = new MetaMessage(0x2F, new byte[]{0x01}, 1);
        MidiEvent ev2 = new MidiEvent(mm, 480L);
        check(ev2.getMessage() == mm, "meta message identity");
        check(ev2.getTick() == 480L, "meta event tick");

        // Two events over the same message are independent in tick.
        MidiEvent a = new MidiEvent(sm, 1L);
        MidiEvent b = new MidiEvent(sm, 2L);
        a.setTick(100L);
        check(a.getTick() == 100L && b.getTick() == 2L, "independent tick state");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: tick identity/mutation/negative ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.ShortMessage msg = new javax.sound.midi.ShortMessage(0x90, 30, 40);
        javax.sound.midi.MidiEvent ev = new javax.sound.midi.MidiEvent(msg, 123);
        check(ev.getTick() == 123, "tick round-trip");
        check(ev.getMessage() == msg, "message same reference");
        ev.setTick(999);
        check(ev.getTick() == 999, "setTick");
        javax.sound.midi.MidiEvent neg = new javax.sound.midi.MidiEvent(msg, -5);
        check(neg.getTick() == -5, "negative tick accepted");
        javax.sound.midi.MidiEvent zero = new javax.sound.midi.MidiEvent(msg, 0);
        check(zero.getTick() == 0, "zero tick");
        check(javax.sound.midi.MidiEvent.class.getConstructors().length == 1, "single public ctor");
        javax.sound.midi.MidiEvent nullMsg = new javax.sound.midi.MidiEvent(null, 0);
        check(nullMsg.getMessage() == null && nullMsg.getTick() == 0, "null message accepted (no validation), getMessage null");
    }

    static Object serRoundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(o);
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static void expectIMD(String what, Runnable r, String msg) {
        RuntimeException caught = null;
        try { r.run(); }
        catch (RuntimeException e) { caught = e; }
        if (caught == null) throw new AssertionError("expected InvalidMidiDataException: " + what);
        Throwable t = caught.getCause() != null ? caught.getCause() : caught;
        check(t instanceof javax.sound.midi.InvalidMidiDataException, "IMD type for " + what + " got " + t);
        if (t instanceof javax.sound.midi.InvalidMidiDataException) {
            javax.sound.midi.InvalidMidiDataException imd = (javax.sound.midi.InvalidMidiDataException) t;
            check(msg == null || msg.equals(imd.getMessage()), "IMD msg " + what + " [" + imd.getMessage() + "]");
        }
    }
    static void expectIAE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected IAE: " + what); }
        catch (IllegalArgumentException e) { check(msg == null || msg.equals(e.getMessage()), "IAE msg " + what + " [" + e.getMessage() + "]"); }
    }
    static void expectNPE(String what, Runnable r, String msg) {
        try { r.run(); throw new AssertionError("expected NPE: " + what); }
        catch (NullPointerException e) { check(msg == null || msg.equals(e.getMessage()), "NPE msg " + what + " [" + e.getMessage() + "]"); }
    }
    // minimal hand-built SMF (format 0, 1 track, PPQ 96): NOTE_ON tick 0, NOTE_OFF tick 48, EOT
    static byte[] smf0() throws java.io.IOException {
        java.io.ByteArrayOutputStream f = new java.io.ByteArrayOutputStream();
        f.write('M'); f.write('T'); f.write('h'); f.write('d');
        f.write(0); f.write(0); f.write(0); f.write(6);
        f.write(0); f.write(0); f.write(0); f.write(1); f.write(0); f.write(96);
        java.io.ByteArrayOutputStream t = new java.io.ByteArrayOutputStream();
        t.write(0); t.write(0x90); t.write(60); t.write(100);
        t.write(0x30); t.write(0x80); t.write(60); t.write(0);
        t.write(0); t.write(0xFF); t.write(0x2F); t.write(0);
        byte[] tb = t.toByteArray();
        f.write('M'); f.write('T'); f.write('r'); f.write('k');
        f.write((tb.length >> 24) & 0xFF); f.write((tb.length >> 16) & 0xFF); f.write((tb.length >> 8) & 0xFF); f.write(tb.length & 0xFF);
        f.write(tb);
        return f.toByteArray();
    }

}
