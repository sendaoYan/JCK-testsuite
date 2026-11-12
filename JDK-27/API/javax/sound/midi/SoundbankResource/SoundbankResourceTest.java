/*
 * @test
 * @key sound
 * @summary javax.sound.midi.SoundbankResource conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main SoundbankResourceTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

public class SoundbankResourceTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.SoundbankResource");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getSoundbank"), "method getSoundbank");
        check(methods.contains("getName"), "method getName");
        check(methods.contains("getDataClass"), "method getDataClass");
        check(methods.contains("getData"), "method getData");
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB();
        Object payload = new byte[]{10, 20, 30};
        Res r = new Res(sb, "Layer 1", byte[].class, payload);
        check(r.getSoundbank() == sb, "getSoundbank round-trip");
        check("Layer 1".equals(r.getName()), "getName round-trip");
        check(r.getDataClass() == byte[].class, "getDataClass round-trip");
        check(r.getData() == payload, "getData returns payload");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + Gervill real resources ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.SoundbankResource rec = new javax.sound.midi.SoundbankResource(null, "Res", javax.sound.sampled.AudioInputStream.class) {
            public Object getData() { return null; }
        };
        check("Res".equals(rec.getName()), "name");
        check(rec.getDataClass() == javax.sound.sampled.AudioInputStream.class, "dataClass");
        check(rec.getSoundbank() == null, "null soundbank");
        check(rec.getData() == null, "data");
        try {
            javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
            syn.open();
            try {
                javax.sound.midi.Soundbank sbk = syn.getDefaultSoundbank();
                javax.sound.midi.SoundbankResource[] res = sbk.getResources();
                check(res.length == 76, "76 resources in Emergency set");
                check(res[0].getSoundbank() == sbk, "resource bound to soundbank");
                boolean hasNamed = false;
                for (javax.sound.midi.SoundbankResource r : res) if (r.getName() != null) hasNamed = true;
                check(hasNamed, "resources named");
            } finally { syn.close(); }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            throw new jtreg.SkippedException("no audio device/line available");
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

class Res extends SoundbankResource {
    private final Object data;
    Res(Soundbank sb, String name, Class<?> dc, Object d) { super(sb, name, dc); data = d; }
    public Object getData() { return data; }
}

class FakeSB implements Soundbank {
    public String getName() { return "SB"; }
    public String getVersion() { return "1"; }
    public String getVendor() { return "V"; }
    public String getDescription() { return "D"; }
    public SoundbankResource[] getResources() { return new SoundbankResource[0]; }
    public javax.sound.midi.Instrument[] getInstruments() { return new javax.sound.midi.Instrument[0]; }
    public javax.sound.midi.Instrument getInstrument(javax.sound.midi.Patch p) { return null; }
}
