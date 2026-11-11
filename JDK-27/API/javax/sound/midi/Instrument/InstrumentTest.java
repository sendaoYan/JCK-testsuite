/*
 * @test
 * @key sound
 * @summary javax.sound.midi.Instrument conformance: white-box structure + black-box behavior.
 * @run main InstrumentTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

public class InstrumentTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Instrument");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.SoundbankResource"), "extends SoundbankResource");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        boolean hasGetPatch = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals("getPatch")) hasGetPatch = true;
        check(hasGetPatch, "declares getPatch");
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB("Bank", "1.0", "Vendor", "desc");
        Patch p = new Patch(3, 42);
        Inst inst = new Inst(sb, p, "Grand Piano", byte[].class);
        check(inst.getPatch() == p, "getPatch returns the constructed Patch instance");
        check(inst.getPatch().getBank() == 3, "Patch bank round-trip");
        check(inst.getPatch().getProgram() == 42, "Patch program round-trip");
        check("Grand Piano".equals(inst.getName()), "inherited getName round-trip");
        check(inst.getDataClass() == byte[].class, "inherited getDataClass round-trip");
        check(inst.getSoundbank() == sb, "inherited getSoundbank round-trip");
        check(inst.getData() != null, "getData returns resource payload");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + real GM instrument surface ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Patch patch = new javax.sound.midi.Patch(1, 2);
        javax.sound.midi.Instrument rec = new javax.sound.midi.Instrument(null, patch, "RecInst", null) {
            public Object getData() { return "payload"; }
        };
        check("RecInst".equals(rec.getName()), "name passthrough");
        check(rec.getPatch().getBank() == 1 && rec.getPatch().getProgram() == 2, "patch passthrough");
        check(rec.getSoundbank() == null, "null soundbank allowed");
        check("payload".equals(rec.getData()), "custom data");
        try {
            javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
            syn.open();
            try {
                javax.sound.midi.Instrument piano = syn.getDefaultSoundbank().getInstruments()[0];
                check("Acoustic Grand Piano".equals(piano.getName()), "GM 0 name");
                check(piano.getSoundbank() == syn.getDefaultSoundbank(), "instrument knows soundbank");
                check(piano.toString().startsWith("Instrument: Acoustic Grand Piano"), "toString format [bank #0 preset #0]: " + piano.toString());
                check(piano.getDataClass() == null, "Gervill instrument dataClass null");
            } finally { syn.close(); }
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

class Inst extends Instrument {
    private final Object data;
    Inst(Soundbank sb, Patch p, String name, Class<?> dc) { super(sb, p, name, dc); this.data = new Object(); }
    public Object getData() { return data; }
}

class FakeSB implements Soundbank {
    private final String name, version, vendor, description;
    FakeSB(String n, String v, String vd, String d) { name = n; version = v; vendor = vd; description = d; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getVendor() { return vendor; }
    public String getDescription() { return description; }
    public SoundbankResource[] getResources() { return new SoundbankResource[0]; }
    public Instrument[] getInstruments() { return new Instrument[0]; }
    public Instrument getInstrument(Patch p) { return null; }
}
