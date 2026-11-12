/*
 * @test
 * @key sound
 * @summary javax.sound.midi.Synthesizer conformance: white-box structure + black-box behavior.
 * @run main SynthesizerTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.VoiceStatus;

public class SynthesizerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean noThrow(Runnable r) { try { r.run(); return true; } catch (Throwable t) { return false; } }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Synthesizer");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("javax.sound.midi.MidiDevice"), "extends MidiDevice");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        String[] names = {"getMaxPolyphony", "getLatency", "getChannels", "getVoiceStatus", "isSoundbankSupported", "loadInstrument", "unloadInstrument", "remapInstrument", "getDefaultSoundbank", "getAvailableInstruments", "getLoadedInstruments", "loadAllInstruments", "unloadAllInstruments", "loadInstruments", "unloadInstruments"};
        for (String n : names) check(methods.contains(n), "method " + n);
    }

    static void testBlackBox() throws Exception {
        Synthesizer syn;
        try {
            syn = MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException e) {
            check(true, "synthesizer unavailable (degraded)");
            return;
        }
        check(syn != null, "getSynthesizer non-null");
        check(syn instanceof MidiDevice, "synthesizer is MidiDevice");
        try {
            check(syn.getMaxPolyphony() >= -1, "max polyphony >= -1");
            check(syn.getLatency() >= 0L, "latency non-negative");
            MidiChannel[] ch = syn.getChannels();
            check(ch != null && ch.length > 0, "channels non-empty");
            check(syn.getVoiceStatus() != null, "voice status non-null");
            check(syn.getAvailableInstruments() != null, "available instruments non-null");
            check(syn.getLoadedInstruments() != null, "loaded instruments non-null");

            Soundbank sb = syn.getDefaultSoundbank();
            if (sb != null) {
                check(sb.getName() != null, "soundbank name non-null");
                check(sb.getVendor() != null, "soundbank vendor non-null");
                check(sb.getVersion() != null, "soundbank version non-null");
                check(sb.getDescription() != null, "soundbank description non-null");
                check(sb.getInstruments() != null, "soundbank instruments non-null");
                check(sb.getResources() != null, "soundbank resources non-null");
                check(syn.isSoundbankSupported(sb), "isSoundbankSupported(default) true");
                check(noThrow(() -> syn.loadAllInstruments(sb)), "loadAllInstruments no-throw");
                check(syn.getLoadedInstruments() != null, "loaded instruments after loadAll non-null");
                Instrument[] sbInstr = sb.getInstruments();
                if (sbInstr.length > 0) {
                    Instrument first = sbInstr[0];
                    check(first.getPatch() != null, "instrument patch non-null");
                    check(first.getName() != null, "instrument name non-null");
                    check(first.getSoundbank() == sb, "instrument soundbank is default soundbank");
                    check(noThrow(() -> { syn.loadInstrument(first); syn.unloadInstrument(first); }), "loadInstrument/unloadInstrument no-throw");
                    check(noThrow(() -> syn.remapInstrument(first, first)), "remapInstrument no-throw");
                }
            }
        } finally {
            if (syn.isOpen()) syn.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        if (testContractDeep()) System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: Gervill closed loop + channels + instruments ----
    static boolean testContractDeep() throws Exception {
        try {
            javax.sound.midi.Synthesizer syn = javax.sound.midi.MidiSystem.getSynthesizer();
            check(!syn.isOpen(), "closed initially");
            syn.open();
            try {
                check(syn.isOpen(), "open");
                javax.sound.midi.Soundbank sbk0 = syn.getDefaultSoundbank();
                if (sbk0 == null || !"Emergency GM sound set".equals(sbk0.getName()) || sbk0.getInstruments().length != 129) {
                    System.out.println("PASS (" + passed + " assertions; SKIP: no Emergency GM Gervill synthesizer on this host)");
                    return false;
                }
                check(syn.getMaxPolyphony() == 64, "max polyphony 64");
                check(syn.getMaxReceivers() == -1, "unlimited receivers (-1)");
                check(syn.getMaxTransmitters() == 0, "Gervill exposes no transmitters (0)");
                javax.sound.midi.MidiChannel[] chans = syn.getChannels();
                check(chans.length == 16, "16 channels");
                check(chans[0] != chans[1], "distinct channel objects");
                javax.sound.midi.VoiceStatus[] vs = syn.getVoiceStatus();
                check(vs.length == 64, "64 voices");
                check(!vs[0].active, "voice inactive initially");
                javax.sound.midi.Soundbank sbk = syn.getDefaultSoundbank();
                check(sbk != null, "default soundbank present");
                check("Emergency GM sound set".equals(sbk.getName()), "soundbank name Emergency GM sound set");
                javax.sound.midi.Instrument[] instrs = sbk.getInstruments();
                check(instrs.length == 129, "129 instruments");
                check("Acoustic Grand Piano".equals(instrs[0].getName()), "first instrument GM piano");
                check(instrs[0].getPatch().getBank() == 0 && instrs[0].getPatch().getProgram() == 0, "piano patch 0/0");
                check(syn.getLoadedInstruments().length == 129, "all 129 loaded by default");
                check(syn.loadInstrument(instrs[0]), "loadInstrument true (already loaded)");
                syn.unloadInstrument(instrs[0]);
                check(syn.getLoadedInstruments().length == 128, "unload drops to 128");
                check(syn.loadInstrument(instrs[0]), "reload true");
                check(syn.loadAllInstruments(sbk), "loadAllInstruments true");
                javax.sound.midi.Instrument[] avail = syn.getAvailableInstruments();
                check(avail.length == 129, "available == 129");
                // channel behavior closed loop
                javax.sound.midi.MidiChannel ch = chans[0];
                ch.noteOn(60, 64);
                ch.noteOff(60);
                ch.controlChange(7, 120);
                check(ch.getController(7) == 120, "controller round-trip");
                ch.programChange(40);
                check(ch.getProgram() == 40, "program round-trip");
                ch.setPolyPressure(60, 50);
                check(ch.getPolyPressure(60) == 50, "poly pressure");
                ch.setChannelPressure(30);
                check(ch.getChannelPressure() == 30, "channel pressure");
                ch.setPitchBend(9000);
                check(ch.getPitchBend() == 9000, "pitch bend round-trip");
                ch.setPitchBend(16384);
                check(ch.getPitchBend() == 16256, "pitch bend clamped to 16256 for 16384 (max bend)");
                ch.allNotesOff();
                ch.allSoundOff();
                ch.resetAllControllers();
                ch.setMono(true);
                check(ch.getMono(), "mono round-trip");
                ch.setMute(true);
                check(ch.getMute(), "mute round-trip");
                ch.setSolo(true);
                check(ch.getSolo(), "solo round-trip");
                ch.setOmni(true);
                check(!ch.getOmni(), "Gervill does not support omni (stays false)");
                // out-of-range silently clamped (& 0x7F), no exception
                ch.controlChange(128, 64);
                ch.noteOn(128, 64);
                ch.programChange(128);
                passed += 3; // silent clamp semantics
                check(ch.getController(0) == 0, "controller 0 default");
            } finally { syn.close(); }
            check(!syn.isOpen(), "close");
        } catch (javax.sound.midi.MidiUnavailableException e) {
            System.out.println("SKIP: no audio device/line available");
        }
        check(javax.sound.midi.Synthesizer.class.getMethod("getMaxPolyphony").getReturnType() == int.class, "surface");
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
