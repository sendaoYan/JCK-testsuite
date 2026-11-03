/*
 * @test
 * @summary javax.sound.midi.MidiDeviceReceiver conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main MidiDeviceReceiverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class MidiDeviceReceiverTest {
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
        Class<?> c = Class.forName("javax.sound.midi.MidiDeviceReceiver");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("javax.sound.midi.Receiver"), "extends javax.sound.midi.Receiver");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "declared method count");
        check(ms[0].getName().equals("getMidiDevice"), "method getMidiDevice");
        check(ms[0].getReturnType().getName().equals("javax.sound.midi.MidiDevice"), "getMidiDevice return type");
        check(ms[0].getParameterTypes().length == 0, "getMidiDevice no params");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Receiver r;
        try {
            r = MidiSystem.getReceiver();
        } catch (Throwable t) {
            // No receiver available: degrade to white-box only.
            return;
        }
        boolean closed = false;
        try {
            check(r instanceof MidiDeviceReceiver, "getReceiver returns MidiDeviceReceiver");
            MidiDeviceReceiver mdr = (MidiDeviceReceiver) r;
            MidiDevice dev = mdr.getMidiDevice();
            check(dev != null, "getMidiDevice non-null");
            check(mdr.getMidiDevice() == dev, "getMidiDevice identity across calls");

            ShortMessage sm = new ShortMessage();
            sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
            noThrow(() -> mdr.send(sm, -1), "send via MidiDeviceReceiver");
            noThrow(() -> mdr.close(), "close");
            closed = true;
        } finally {
            if (!closed) { try { r.close(); } catch (Throwable ignored) {} }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
