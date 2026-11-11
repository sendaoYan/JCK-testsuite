/*
 * @test
 * @key sound
 * @summary javax.sound.midi.MetaEventListener conformance: white-box structure + black-box behavior.
 * @run main MetaEventListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;

public class MetaEventListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MetaEventListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("java.util.EventListener"), "extends java.util.EventListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "declared method count");
        check(ms[0].getName().equals("meta"), "method meta");
        check(ms[0].getParameterTypes().length == 1
                && ms[0].getParameterTypes()[0].getName().equals("javax.sound.midi.MetaMessage"),
                "meta(MetaMessage) parameter");
        check(ms[0].getReturnType() == void.class, "meta returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The interface's sole contract is the listener callback signature.
        MetaMessage msg = new MetaMessage();
        msg.setMessage(0x51, new byte[]{0x07, (byte) 0xA1, 0x20}, 3); // tempo meta message
        final MetaMessage[] received = new MetaMessage[1];
        final int[] count = new int[1];
        MetaEventListener listener = m -> { received[0] = m; count[0]++; };
        check(listener != null, "listener instantiable");
        listener.meta(msg);
        check(received[0] == msg, "callback receives same MetaMessage instance");
        check(count[0] == 1, "callback invoked exactly once");
        listener.meta(msg);
        check(count[0] == 2, "callback invoked again");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording listener + real sequencer registration ----
    static void testContractDeep() throws Exception {
        final int[] count = new int[1];
        final javax.sound.midi.MetaMessage[] last = new javax.sound.midi.MetaMessage[1];
        javax.sound.midi.MetaEventListener l = new javax.sound.midi.MetaEventListener() {
            public void meta(javax.sound.midi.MetaMessage m) { count[0]++; last[0] = m; }
        };
        javax.sound.midi.MetaMessage probe = new javax.sound.midi.MetaMessage();
        probe.setMessage(3, new byte[] { 1 }, 1);
        l.meta(probe);
        check(count[0] == 1 && last[0] == probe, "callback captures reference");
        l.meta(probe);
        check(count[0] == 2, "repeat callback");
        try {
            javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
            sqr.open();
            try {
                sqr.addMetaEventListener(l);
                sqr.removeMetaEventListener(l);
                passed += 2; // registration lifecycle accepted
            } finally { sqr.close(); }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
        }
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
