/*
 * @test
 * @summary javax.sound.midi.MidiSystem conformance: behavioral.
 * @run main MidiSystemTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MidiSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.midi.MidiDevice.Info[] infos = javax.sound.midi.MidiSystem.getMidiDeviceInfo();
        check(infos != null, "getMidiDeviceInfo non-null");
        int[] types = javax.sound.midi.MidiSystem.getMidiFileTypes();
        check(types != null && types.length > 0, "getMidiFileTypes non-empty");
        check(javax.sound.midi.MidiSystem.isFileTypeSupported(types[0]), "isFileTypeSupported");
        javax.sound.midi.Sequence seq = new javax.sound.midi.Sequence(javax.sound.midi.Sequence.PPQ, 96);
        seq.createTrack();
        check(javax.sound.midi.MidiSystem.isFileTypeSupported(types[0], seq), "isFileTypeSupported(seq)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int written = javax.sound.midi.MidiSystem.write(seq, types[0], bos);
        check(written > 0, "write");
        javax.sound.midi.Sequence back = javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getResolution() == 96, "getSequence roundtrip");
        javax.sound.midi.MidiFileFormat fmt = javax.sound.midi.MidiSystem.getMidiFileFormat(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(fmt.getType() == types[0], "getMidiFileFormat");
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: hand-built SMF byte-level parse + write round-trip + device/file contracts ----
    static void testContractDeep() throws Exception {
        byte[] smf = smf0();
        check(smf.length == 34, "SMF fixture 34 bytes");
        javax.sound.midi.Sequence seq = javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(smf));
        check(seq.getDivisionType() == 0.0f && seq.getResolution() == 96, "parsed division/resolution");
        check(seq.getTracks().length == 1, "one track");
        check(seq.getTickLength() == 48, "tickLength 48");
        check(seq.getMicrosecondLength() == 250000, "micros 250000 (48 ticks @ PPQ96 default 120bpm)");
        javax.sound.midi.Track tr = seq.getTracks()[0];
        check(tr.size() == 3, "2 written events + implicit EOT");
        javax.sound.midi.MidiMessage ev0 = tr.get(0).getMessage();
        check(ev0.getStatus() == 0x90 && ev0.getLength() == 3, "ev0 NOTE_ON status");
        javax.sound.midi.ShortMessage sm0 = (javax.sound.midi.ShortMessage) ev0;
        check(sm0.getData1() == 60 && sm0.getData2() == 100 && tr.get(0).getTick() == 0, "ev0 fields tick 0");
        javax.sound.midi.ShortMessage sm1 = (javax.sound.midi.ShortMessage) tr.get(1).getMessage();
        check(sm1.getStatus() == 0x80 && sm1.getData1() == 60 && sm1.getData2() == 0 && tr.get(1).getTick() == 48, "ev1 NOTE_OFF tick 48");
        javax.sound.midi.MetaMessage eot = (javax.sound.midi.MetaMessage) tr.get(2).getMessage();
        check(eot.getType() == 47 && eot.getLength() == 3, "implicit EndOfTrack meta type 47");
        // write->read round-trip both file types
        for (int type = 0; type <= 1; type++) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int written = javax.sound.midi.MidiSystem.write(seq, type, out);
            check(written == 34, "type " + type + " writes 34 bytes");
            check(out.size() == 34, "stream length 34");
            javax.sound.midi.Sequence back = javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(out.toByteArray()));
            check(back.getDivisionType() == 0.0f && back.getResolution() == 96 && back.getTickLength() == 48, "type " + type + " round-trip fields");
            check(back.getTracks().length == 1, "type " + type + " round-trip tracks");
        }
        check(!javax.sound.midi.MidiSystem.isFileTypeSupported(2), "type 2 not supported for writing");
        int[] types = javax.sound.midi.MidiSystem.getMidiFileTypes();
        boolean has0 = false, has1 = false;
        for (int t : types) { if (t == 0) has0 = true; if (t == 1) has1 = true; }
        check(has0 && has1, "file types contain 0 and 1");
        // corrupt inputs
        byte[] bad = smf.clone(); bad[0] = 'X';
        try { javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(bad)); throw new AssertionError("expected IMD"); }
        catch (javax.sound.midi.InvalidMidiDataException e) { check("could not get sequence from input stream".equals(e.getMessage()), "bad header message [" + e.getMessage() + "]"); }
        try { javax.sound.midi.MidiSystem.getSequence(new java.io.ByteArrayInputStream(new byte[] { 1, 2, 3 })); throw new AssertionError("expected EOF"); }
        catch (java.io.EOFException e) { passed++; }
        // device info contract (headless: software devices)
        javax.sound.midi.MidiDevice.Info[] infos = javax.sound.midi.MidiSystem.getMidiDeviceInfo();
        check(infos != null && infos.length == 2, "two software devices (skip reason: no hardware MIDI)");
        boolean gervill = false, rts = false;
        for (javax.sound.midi.MidiDevice.Info i : infos) {
            check(i.getName() != null && i.getVendor() != null && i.getDescription() != null && i.getVersion() != null, "info fields non-null " + i.getName());
            check(i.toString().contains(i.getName()), "toString contains name");
            if ("Gervill".equals(i.getName())) gervill = true;
            if ("Real Time Sequencer".equals(i.getName())) rts = true;
        }
        check(gervill && rts, "Gervill + Real Time Sequencer present");
        check(infos[0].equals(infos[0]) && infos[0].hashCode() == infos[0].hashCode(), "info equals/hashCode identity");
        check(!infos[0].equals(infos[1]), "distinct infos unequal");
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(infos[0]);
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            throw new AssertionError("expected NotSerializableException");
        } catch (java.io.NotSerializableException e) { passed++; }
        // sequencer + synthesizer closed loops (software, no audio hardware needed)
        javax.sound.midi.Sequencer sqr = javax.sound.midi.MidiSystem.getSequencer();
        check(!sqr.isOpen(), "sequencer starts closed");
        sqr.open();
        check(sqr.isOpen(), "open");
        sqr.setSequence(seq);
        check(sqr.getTickLength() == 48, "sequencer tickLength matches sequence");
        check(sqr.getTickPosition() == 0, "position 0");
        sqr.setTempoInBPM(120.0f);
        check(sqr.getTempoInBPM() == 120.0f, "tempo BPM round-trip");
        check(sqr.getTempoFactor() == 1.0f, "tempo factor 1.0");
        sqr.close();
        check(!sqr.isOpen(), "closed");
        javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
        syn.open();
        check(syn.getMaxPolyphony() == 64, "Gervill max polyphony 64");
        check(syn.getChannels().length == 16, "16 channels");
        check(syn.getVoiceStatus().length == 64, "64 voice slots");
        check(syn.getDefaultSoundbank() != null, "default soundbank present");
        check(syn.getLoadedInstruments().length == 129, "129 loaded instruments (Emergency GM set)");
        check(syn.loadAllInstruments(syn.getDefaultSoundbank()), "loadAllInstruments idempotent true");
        syn.close();
        // receiver/transmitter from sequencer
        javax.sound.midi.Receiver rx = javax.sound.midi.MidiSystem.getReceiver();
        rx.send(new javax.sound.midi.ShortMessage(0x90, 60, 64), -1);
        rx.close();
        check(rx != null, "system receiver headless (software)");
        try {
            javax.sound.midi.MidiSystem.getTransmitter();
            check(false, "default transmitter should be unavailable headless");
        } catch (javax.sound.midi.MidiUnavailableException e) { check(e.getMessage() == null, "MidiUnavailableException null message (no default transmitter device)"); }
        // bad soundbank bytes
        try { javax.sound.midi.MidiSystem.getSoundbank(new java.io.ByteArrayInputStream(new byte[] { 1 })); throw new AssertionError("expected EOF"); }
        catch (java.io.EOFException e) { passed++; }
        // midi file format parse from same bytes
        javax.sound.midi.MidiFileFormat ff = javax.sound.midi.MidiSystem.getMidiFileFormat(new java.io.ByteArrayInputStream(smf));
        check(ff.getType() == 0, "getMidiFileFormat type");
        int[] perSeq = javax.sound.midi.MidiSystem.getMidiFileTypes(seq);
        check(perSeq.length >= 1, "types for concrete sequence");
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
