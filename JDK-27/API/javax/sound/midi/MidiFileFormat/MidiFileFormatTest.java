/*
 * @test
 * @summary javax.sound.midi.MidiFileFormat conformance: white-box structure + black-box behavior.
 * @run main MidiFileFormatTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequence;

public class MidiFileFormatTest {
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
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiFileFormat");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(MidiFileFormat.UNKNOWN_LENGTH == -1, "UNKNOWN_LENGTH constant");
        Set<String> dm = methodNames(c);
        check(dm.contains("getType"), "method getType");
        check(dm.contains("getDivisionType"), "method getDivisionType");
        check(dm.contains("getResolution"), "method getResolution");
        check(dm.contains("getByteLength"), "method getByteLength");
        check(dm.contains("getMicrosecondLength"), "method getMicrosecondLength");
        check(dm.contains("properties"), "method properties");
        check(dm.contains("getProperty"), "method getProperty");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "constructor count");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MidiFileFormat ff = new MidiFileFormat(1, Sequence.PPQ, 480, 100, 5000L);
        check(ff.getType() == 1, "getType");
        check(ff.getDivisionType() == Sequence.PPQ, "getDivisionType");
        check(ff.getResolution() == 480, "getResolution");
        check(ff.getByteLength() == 100, "getByteLength");
        check(ff.getMicrosecondLength() == 5000L, "getMicrosecondLength");
        check(ff.properties() != null, "properties non-null");
        check(ff.properties().isEmpty(), "no-property format returns empty map");
        check(ff.getProperty("absent") == null, "getProperty absent returns null");

        // Unknown lengths.
        MidiFileFormat unknown = new MidiFileFormat(0, Sequence.PPQ, 96,
                MidiFileFormat.UNKNOWN_LENGTH, MidiFileFormat.UNKNOWN_LENGTH);
        check(unknown.getByteLength() == -1, "UNKNOWN_LENGTH byteLength");
        check(unknown.getMicrosecondLength() == -1, "UNKNOWN_LENGTH microsecondLength");

        // Properties constructor round-trip + unmodifiable copy.
        Map<String, Object> props = new HashMap<>();
        props.put("author", "Alice");
        props.put("title", "Song");
        MidiFileFormat ff2 = new MidiFileFormat(2, Sequence.PPQ, 192, 2048, 123456L, props);
        check("Alice".equals(ff2.getProperty("author")), "getProperty author");
        check("Song".equals(ff2.getProperty("title")), "getProperty title");
        check(ff2.getProperty("missing") == null, "getProperty missing");
        check(ff2.getType() == 2, "properties ctor type");
        check(ff2.getResolution() == 192, "properties ctor resolution");

        Map<String, Object> returned = ff2.properties();
        check(returned.containsKey("author") && returned.containsKey("title"), "properties map contents");
        expectThrows(UnsupportedOperationException.class, () -> returned.put("x", "y"), "properties unmodifiable");

        // Mutating the source map after construction does not affect the format.
        props.put("author", "Bob");
        check("Alice".equals(ff2.getProperty("author")), "constructor copies source map");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: both ctor forms / properties map immutability / UNKNOWN ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.MidiFileFormat ff = new javax.sound.midi.MidiFileFormat(0, javax.sound.midi.Sequence.PPQ, 96, 340, 250000L);
        check(ff.getType() == 0 && ff.getDivisionType() == 0.0f && ff.getResolution() == 96, "5-arg ctor fields");
        check(ff.getByteLength() == 340, "byte length");
        check(ff.getMicrosecondLength() == 250000L, "microsecond length");
        java.util.HashMap<String, Object> props = new java.util.HashMap<String, Object>();
        props.put("title", "deep");
        javax.sound.midi.MidiFileFormat ff2 = new javax.sound.midi.MidiFileFormat(1, javax.sound.midi.Sequence.SMPTE_30, 240, javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH, 1000L, props);
        check(ff2.getType() == 1 && ff2.getDivisionType() == 30.0f, "6-arg ctor fields");
        check(ff2.getByteLength() == -1, "UNKNOWN_LENGTH passthrough");
        check("deep".equals(ff2.getProperty("title")), "property get");
        check(ff2.getProperty("missing") == null, "missing property null");
        java.util.Map<String, Object> view = ff2.properties();
        check("deep".equals(view.get("title")), "properties map view");
        boolean immutable = false;
        try { view.put("x", "y"); } catch (UnsupportedOperationException e) { immutable = true; }
        check(immutable, "properties map unmodifiable");
        check(javax.sound.midi.MidiFileFormat.UNKNOWN_LENGTH == -1, "UNKNOWN_LENGTH -1");
        javax.sound.midi.MidiFileFormat negType = new javax.sound.midi.MidiFileFormat(-1, javax.sound.midi.Sequence.PPQ, 96, 1, 1L);
        check(negType.getType() == -1, "negative type accepted without validation");
        javax.sound.midi.MidiFileFormat parsed = javax.sound.midi.MidiSystem.getMidiFileFormat(new java.io.ByteArrayInputStream(smf0()));
        check(parsed.getType() == 0 && parsed.getResolution() == 96, "parsed format from hand-built SMF");
        check(parsed.getDivisionType() == 0.0f, "parsed division PPQ");
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
