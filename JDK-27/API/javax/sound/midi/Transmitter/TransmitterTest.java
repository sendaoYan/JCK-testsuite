/*
 * @test
 * @summary javax.sound.midi.Transmitter conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main TransmitterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

public class TransmitterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void noThrow(ThrowingRunnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            throw new AssertionError(msg + " threw " + t);
        }
        check(true, msg);
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Transmitter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 3, "declared method count");
        Set<String> dm = new HashSet<>();
        for (Method m : ms) dm.add(m.getName());
        check(dm.contains("setReceiver"), "method setReceiver");
        check(dm.contains("getReceiver"), "method getReceiver");
        check(dm.contains("close"), "method close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Sequencer seq;
        Transmitter t;
        try {
            seq = MidiSystem.getSequencer();
            t = seq.getTransmitter();
        } catch (Throwable ex) {
            // No transmitter available: degrade to white-box only.
            return;
        }
        try {
            check(t != null, "transmitter non-null");

            final int[] sent = new int[1];
            Receiver stub = new Receiver() {
                public void send(MidiMessage message, long timeStamp) { sent[0]++; }
                public void close() {}
            };
            t.setReceiver(stub);
            check(t.getReceiver() == stub, "setReceiver/getReceiver round-trip");
            t.setReceiver(null);
            check(t.getReceiver() == null, "setReceiver(null)");

            noThrow(() -> t.close(), "close");
        } finally {
            try { if (seq != null) seq.close(); } catch (Throwable ignored) {}
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
