/*
 * @test
 * @summary javax.sound.midi.ControllerEventListener conformance: white-box structure + black-box behavior.
 * @run main ControllerEventListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.ShortMessage;

public class ControllerEventListenerTest {
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
        Class<?> c = Class.forName("javax.sound.midi.ControllerEventListener");
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
        check(ms[0].getName().equals("controlChange"), "method controlChange");
        check(ms[0].getParameterTypes().length == 1
                && ms[0].getParameterTypes()[0].getName().equals("javax.sound.midi.ShortMessage"),
                "controlChange(ShortMessage) parameter");
        check(ms[0].getReturnType() == void.class, "controlChange returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The interface's sole contract is the listener callback signature.
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 7, 100);
        final ShortMessage[] received = new ShortMessage[1];
        final int[] count = new int[1];
        ControllerEventListener listener = m -> { received[0] = m; count[0]++; };
        check(listener != null, "listener instantiable");
        listener.controlChange(msg);
        check(received[0] == msg, "callback receives same ShortMessage instance");
        check(count[0] == 1, "callback invoked exactly once");
        // Multiple invocations accumulate.
        listener.controlChange(msg);
        check(count[0] == 2, "callback invoked again");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: controllers() array contract + registration ----
    static void testContractDeep() throws Exception {
        final int[] count = new int[1];
        javax.sound.midi.ControllerEventListener l = new javax.sound.midi.ControllerEventListener() {
            public void controlChange(javax.sound.midi.ShortMessage m) { count[0]++; }
        };
        javax.sound.midi.ShortMessage msg = new javax.sound.midi.ShortMessage(0xB0, 7, 120);
        l.controlChange(msg);
        check(count[0] == 1, "callback");
        javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
        sqr.open();
        try {
            int[] empty = sqr.addControllerEventListener(l, new int[0]);
            check(empty != null, "empty controllers returns array");
            int[] all = sqr.addControllerEventListener(l, new int[] { 7, 10 });
            check(all != null, "subscribe returns previous controllers");
            int[] none = sqr.removeControllerEventListener(l, new int[] { 7 });
            check(none != null, "unsubscribe returns remaining");
        } finally { sqr.close(); }
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
