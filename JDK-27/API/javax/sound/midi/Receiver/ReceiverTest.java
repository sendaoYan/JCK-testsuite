/*
 * @test
 * @summary javax.sound.midi.Receiver conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main ReceiverTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class ReceiverTest {
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
        Class<?> c = Class.forName("javax.sound.midi.Receiver");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("java.lang.AutoCloseable"), "extends java.lang.AutoCloseable");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "declared method count");
        Set<String> dm = new HashSet<>();
        for (Method m : ms) dm.add(m.getName());
        check(dm.contains("send"), "method send");
        check(dm.contains("close"), "method close");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Receiver recv;
        try {
            recv = MidiSystem.getReceiver();
        } catch (Throwable t) {
            // No receiver available: degrade to white-box only.
            return;
        }
        ShortMessage sm = new ShortMessage();
        sm.setMessage(ShortMessage.NOTE_ON, 0, 60, 100);
        MetaMessage mm = new MetaMessage(0x2F, new byte[]{0x01, 0x02, 0x03}, 3);

        boolean closed = false;
        try {
            check(recv != null, "receiver non-null");
            noThrow(() -> recv.send(sm, -1), "send ShortMessage");
            noThrow(() -> recv.send(mm, 0), "send MetaMessage");
            noThrow(() -> recv.close(), "close");
            closed = true;
        } finally {
            if (!closed) { try { recv.close(); } catch (Throwable ignored) {} }
        }

        // AutoCloseable contract via try-with-resources on a fresh receiver.
        noThrow(() -> { try (Receiver r2 = MidiSystem.getReceiver()) { r2.send(sm, -1); } },
                "try-with-resources close");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
