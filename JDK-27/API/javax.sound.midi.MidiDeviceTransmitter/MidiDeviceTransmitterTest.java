/*
 * @test
 * @summary javax.sound.midi.MidiDeviceTransmitter conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main MidiDeviceTransmitterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;

public class MidiDeviceTransmitterTest {
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
        Class<?> c = Class.forName("javax.sound.midi.MidiDeviceTransmitter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("javax.sound.midi.Transmitter"), "extends javax.sound.midi.Transmitter");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "declared method count");
        check(ms[0].getName().equals("getMidiDevice"), "method getMidiDevice");
        check(ms[0].getReturnType().getName().equals("javax.sound.midi.MidiDevice"), "getMidiDevice return type");
        check(ms[0].getParameterTypes().length == 0, "getMidiDevice no params");
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
            check(t instanceof MidiDeviceTransmitter, "getTransmitter returns MidiDeviceTransmitter");
            MidiDeviceTransmitter mdt = (MidiDeviceTransmitter) t;
            MidiDevice dev = mdt.getMidiDevice();
            check(dev != null, "getMidiDevice non-null");
            check(mdt.getMidiDevice() == dev, "getMidiDevice identity across calls");

            Receiver stub = new Receiver() {
                public void send(MidiMessage message, long timeStamp) {}
                public void close() {}
            };
            mdt.setReceiver(stub);
            check(mdt.getReceiver() == stub, "setReceiver/getReceiver round-trip");

            noThrow(() -> mdt.close(), "close");
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
