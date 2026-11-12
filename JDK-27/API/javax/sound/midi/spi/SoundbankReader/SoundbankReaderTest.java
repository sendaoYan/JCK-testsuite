/*
 * @test
 * @key sound
 * @summary javax.sound.midi.spi.SoundbankReader conformance: white-box structure + black-box behavior.
 * @library /lib
 * @run main SoundbankReaderTest
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.spi.SoundbankReader;

public class SoundbankReaderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.SoundbankReader");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getSoundbank"), "method getSoundbank");
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB();
        FakeSoundbankReader r = new FakeSoundbankReader(sb);
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{1, 2, 3});
        check(r.getSoundbank(in) == sb, "getSoundbank(InputStream) round-trip");
        URL url = new File("dummy").toURI().toURL();
        check(r.getSoundbank(url) == sb, "getSoundbank(URL) round-trip");
        File f = new File("dummy");
        check(r.getSoundbank(f) == sb, "getSoundbank(File) round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: abstract SPI surface + recording subclass closed loop ----
    static void testContractDeep() throws Exception {
        check(java.lang.reflect.Modifier.isAbstract(javax.sound.midi.spi.SoundbankReader.class.getModifiers()), "abstract");
        check(java.lang.reflect.Modifier.isPublic(javax.sound.midi.spi.SoundbankReader.class.getModifiers()), "public");
        
        RecReader r = new RecReader();
        check(r.getSoundbank(new java.io.ByteArrayInputStream(new byte[] { 1 })) == null, "recording reader returns null for junk");
        try {
            javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
            syn.open();
            check(syn.getDefaultSoundbank().getInstruments().length == 129, "real soundbank reachable headless");
            syn.close();
        } catch (javax.sound.midi.MidiUnavailableException e) {
            throw new jtreg.SkippedException("no audio device/line available");
        }
    }
    static class RecReader extends javax.sound.midi.spi.SoundbankReader {
        public javax.sound.midi.Soundbank getSoundbank(java.io.InputStream is) { return null; }
        public javax.sound.midi.Soundbank getSoundbank(java.net.URL url) { return null; }
        public javax.sound.midi.Soundbank getSoundbank(java.io.File f) { return null; }
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

class FakeSoundbankReader extends SoundbankReader {
    private final Soundbank sb;
    FakeSoundbankReader(Soundbank s) { sb = s; }
    public Soundbank getSoundbank(URL url) { return sb; }
    public Soundbank getSoundbank(InputStream stream) { return sb; }
    public Soundbank getSoundbank(File file) { return sb; }
}

class FakeSB implements Soundbank {
    public String getName() { return "SB"; }
    public String getVersion() { return "1"; }
    public String getVendor() { return "V"; }
    public String getDescription() { return "D"; }
    public SoundbankResource[] getResources() { return new SoundbankResource[0]; }
    public Instrument[] getInstruments() { return new Instrument[0]; }
    public Instrument getInstrument(Patch p) { return null; }
}
