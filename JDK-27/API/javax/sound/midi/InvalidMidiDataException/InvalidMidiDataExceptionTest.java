/*
 * @test
 * @summary javax.sound.midi.InvalidMidiDataException conformance: white-box structure + black-box behavior.
 * @run main InvalidMidiDataExceptionTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.sound.midi.InvalidMidiDataException;

public class InvalidMidiDataExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.InvalidMidiDataException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Exception"), "superclass Exception");
        check(c.getInterfaces().length == 0, "interface count");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "constructor count");
        boolean hasString = false;
        for (Constructor<?> k : ctors) {
            if (k.getParameterTypes().length == 1 && k.getParameterTypes()[0] == String.class) hasString = true;
        }
        check(hasString, "String constructor present");
        boolean hasSvid = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("serialVersionUID")) hasSvid = true;
        }
        check(hasSvid, "serialVersionUID present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InvalidMidiDataException e = new InvalidMidiDataException();
        check(e != null, "no-arg constructor");
        check(e instanceof Exception, "is Exception");
        check(e.getMessage() == null, "no-arg message null");

        InvalidMidiDataException e2 = new InvalidMidiDataException("bad MIDI data");
        check("bad MIDI data".equals(e2.getMessage()), "String constructor message");

        InvalidMidiDataException e3 = new InvalidMidiDataException("");
        check("".equals(e3.getMessage()), "empty message");

        expectThrows(InvalidMidiDataException.class,
                () -> { throw new InvalidMidiDataException("boom"); },
                "throw/catch round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: constructor chain + serialization ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.InvalidMidiDataException e0 = new javax.sound.midi.InvalidMidiDataException();
        check(e0.getMessage() == null, "no-arg null message");
        javax.sound.midi.InvalidMidiDataException e1 = new javax.sound.midi.InvalidMidiDataException("bad midi");
        check("bad midi".equals(e1.getMessage()), "message ctor");
        check(e1 instanceof Exception, "extends Exception");
        Object rt = serRoundTrip(e1);
        check(rt instanceof javax.sound.midi.InvalidMidiDataException && "bad midi".equals(((javax.sound.midi.InvalidMidiDataException) rt).getMessage()), "serialization round-trip");
        check(e1 instanceof Exception, "Exception type");
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
