/*
 * @test
 * @summary javax.sound.midi.Synthesizer conformance: white-box structure + black-box behavior.
 * @key sound
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
