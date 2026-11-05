/*
 * @test
 * @key sound
 * @summary javax.sound.midi.Soundbank conformance: white-box structure + black-box behavior.
 * @run main SoundbankTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

public class SoundbankTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Soundbank");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        for (String n : new String[]{"getName", "getVersion", "getVendor", "getDescription", "getResources", "getInstruments", "getInstrument"})
            check(methods.contains(n), "method " + n);
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB();
        check("Emergency Bank".equals(sb.getName()), "getName round-trip");
        check("2.1".equals(sb.getVersion()), "getVersion round-trip");
        check("Vendor".equals(sb.getVendor()), "getVendor round-trip");
        check("Description".equals(sb.getDescription()), "getDescription round-trip");
        check(sb.getResources().length == 1, "getResources length");
        check(sb.getInstruments().length == 1, "getInstruments length");
        Instrument inst = sb.getInstruments()[0];
        check(sb.getInstrument(inst.getPatch()) == inst, "getInstrument(patch) returns instrument");
        check(sb.getInstrument(new Patch(99, 99)) == null, "getInstrument unknown patch null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementation + real soundbank equality ----
    static void testContractDeep() throws Exception {
        javax.sound.midi.Soundbank rec = new javax.sound.midi.Soundbank() {
            public String getName() { return "RSB"; }
            public String getVersion() { return "1.0"; }
            public String getVendor() { return "deep"; }
            public String getDescription() { return "recording"; }
            public javax.sound.midi.SoundbankResource[] getResources() { return new javax.sound.midi.SoundbankResource[0]; }
            public javax.sound.midi.Instrument[] getInstruments() { return new javax.sound.midi.Instrument[0]; }
            public javax.sound.midi.Instrument getInstrument(javax.sound.midi.Patch p) { return null; }
        };
        check("RSB".equals(rec.getName()) && "1.0".equals(rec.getVersion()) && "deep".equals(rec.getVendor()), "metadata");
        check(rec.getResources().length == 0 && rec.getInstruments().length == 0, "empty arrays");
        check(rec.getInstrument(new javax.sound.midi.Patch(0, 0)) == null, "lookup null");
        javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
        syn.open();
        try {
            javax.sound.midi.Soundbank sbk = syn.getDefaultSoundbank();
            check("Emergency GM sound set".equals(sbk.getName()), "name");
            check(sbk.getVersion() != null && sbk.getVendor() != null && sbk.getDescription() != null, "metadata non-null");
            javax.sound.midi.Instrument[] instrs = sbk.getInstruments();
            check(instrs.length == 129, "129 instruments");
            check(sbk.getInstrument(instrs[0].getPatch()) == instrs[0], "getInstrument by patch returns same instance");
            check(sbk.getInstrument(new javax.sound.midi.Patch(16383, 16383)) == null, "unknown patch null");
            check(sbk.equals(sbk) && !sbk.equals(rec), "identity equals");
            check(sbk.getResources().length == 76, "76 resources");
        } finally { syn.close(); }
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

class FakeSB implements Soundbank {
    private final Instrument inst;
    private final SoundbankResource res;
    FakeSB() {
        inst = new FakeInstrument(this, new Patch(0, 1), "Piano", null);
        res = new FakeResource(this, "Wave", byte[].class);
    }
    public String getName() { return "Emergency Bank"; }
    public String getVersion() { return "2.1"; }
    public String getVendor() { return "Vendor"; }
    public String getDescription() { return "Description"; }
    public SoundbankResource[] getResources() { return new SoundbankResource[]{res}; }
    public Instrument[] getInstruments() { return new Instrument[]{inst}; }
    public Instrument getInstrument(Patch p) {
        Patch ip = inst.getPatch();
        return (ip.getBank() == p.getBank() && ip.getProgram() == p.getProgram()) ? inst : null;
    }
}

class FakeInstrument extends Instrument {
    FakeInstrument(Soundbank sb, Patch p, String name, Class<?> dc) { super(sb, p, name, dc); }
    public Object getData() { return null; }
}

class FakeResource extends SoundbankResource {
    FakeResource(Soundbank sb, String name, Class<?> dc) { super(sb, name, dc); }
    public Object getData() { return null; }
}
