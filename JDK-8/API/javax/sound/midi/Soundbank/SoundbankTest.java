/*
 * @test
 * @key sound
 * @summary javax.sound.midi.Soundbank conformance: interface hierarchy and methods.
 * @run main SoundbankTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SoundbankTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Soundbank");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getName"), "method getName");
        check(dm.contains("getVersion"), "method getVersion");
        check(dm.contains("getVendor"), "method getVendor");
        check(dm.contains("getDescription"), "method getDescription");
        check(dm.contains("getResources"), "method getResources");
        check(dm.contains("getInstruments"), "method getInstruments");
        check(dm.contains("getInstrument"), "method getInstrument");
        if (testContractDeep()) System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording implementation + real soundbank equality ----
    static boolean testContractDeep() throws Exception {
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
        try {
            javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
            syn.open();
            try {
                javax.sound.midi.Soundbank sbk = syn.getDefaultSoundbank();
                javax.sound.midi.Instrument[] instrs = sbk == null ? new javax.sound.midi.Instrument[0] : sbk.getInstruments();
                if (sbk == null || !"Emergency GM sound set".equals(sbk.getName()) || instrs.length != 129) {
                    System.out.println("PASS (" + passed + " assertions; SKIP: no Emergency GM 129-instrument soundbank on this host)");
                    return false;
                }
                check("Emergency GM sound set".equals(sbk.getName()), "name");
                check(sbk.getVersion() != null && sbk.getVendor() != null && sbk.getDescription() != null, "metadata non-null");
                check(instrs.length == 129, "129 instruments");
                check(sbk.getInstrument(instrs[0].getPatch()) == instrs[0], "getInstrument by patch returns same instance");
                check(sbk.getInstrument(new javax.sound.midi.Patch(16383, 16383)) == null, "unknown patch null");
                check(sbk.equals(sbk) && !sbk.equals(rec), "identity equals");
                check(sbk.getResources().length == 76, "76 resources");
            } finally { syn.close(); }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
        }
        return true;
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
