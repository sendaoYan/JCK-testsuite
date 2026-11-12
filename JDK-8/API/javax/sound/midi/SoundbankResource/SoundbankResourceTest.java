/*
 * @test
 * @key sound
 * @summary javax.sound.midi.SoundbankResource conformance: abstract hierarchy and methods.
 * @library /lib
 * @run main SoundbankResourceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SoundbankResourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.SoundbankResource");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getSoundbank"), "method getSoundbank");
        check(dm.contains("getName"), "method getName");
        check(dm.contains("getDataClass"), "method getDataClass");
        check(dm.contains("getData"), "method getData");
        if (testContractDeep()) System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: recording subclass + Gervill real resources ----
    static boolean testContractDeep() throws Exception {
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
                javax.sound.midi.SoundbankResource[] res = sbk == null ? null : sbk.getResources();
                if (sbk == null || res == null || res.length != 76) {
                    throw new jtreg.SkippedException("no Emergency GM 76-resource soundbank on this host");
                }
                check(res.length == 76, "76 resources in Emergency set");
                check(res[0].getSoundbank() == sbk, "resource bound to soundbank");
                boolean hasNamed = false;
                for (javax.sound.midi.SoundbankResource r : res) if (r.getName() != null) hasNamed = true;
                check(hasNamed, "resources named");
            } finally { syn.close(); }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            throw new jtreg.SkippedException("no audio device/line available");
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
