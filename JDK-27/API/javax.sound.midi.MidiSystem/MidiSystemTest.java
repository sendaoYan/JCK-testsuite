/*
 * @test
 * @summary javax.sound.midi.MidiSystem conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main MidiSystemTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

public class MidiSystemTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()), msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiSystem");
        check(!c.isInterface(), "is not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "is not abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        boolean privateCtor = false;
        for (java.lang.reflect.Constructor<?> ctor : c.getDeclaredConstructors()) if (Modifier.isPrivate(ctor.getModifiers())) privateCtor = true;
        check(privateCtor, "private constructor (not instantiable)");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (Modifier.isStatic(m.getModifiers())) methods.add(m.getName());
        for (String n : new String[]{"getMidiDeviceInfo", "getMidiDevice", "getReceiver", "getTransmitter", "getSynthesizer", "getSequencer", "getSoundbank", "getMidiFileFormat", "getSequence", "getMidiFileTypes", "isFileTypeSupported", "write"})
            check(methods.contains(n), "static method " + n);
    }

    static void testBlackBox() throws Exception {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        check(infos != null, "getMidiDeviceInfo non-null array");

        int[] types = MidiSystem.getMidiFileTypes();
        check(types != null && types.length > 0, "getMidiFileTypes non-empty");
        check(MidiSystem.isFileTypeSupported(types[0]), "isFileTypeSupported(type)");
        Sequence seq = new Sequence(Sequence.PPQ, 96);
        seq.createTrack();
        check(MidiSystem.isFileTypeSupported(types[0], seq), "isFileTypeSupported(type, seq)");
        check(MidiSystem.getMidiFileTypes(seq) != null, "getMidiFileTypes(seq) non-null");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int written = MidiSystem.write(seq, types[0], bos);
        check(written > 0, "write returns positive byte count");

        Sequence back = MidiSystem.getSequence(new ByteArrayInputStream(bos.toByteArray()));
        check(back != null, "getSequence round-trip non-null");
        check(back.getResolution() == 96, "getSequence round-trip resolution");
        check(back.getDivisionType() == Sequence.PPQ, "getSequence round-trip division type");

        MidiFileFormat fmt = MidiSystem.getMidiFileFormat(new ByteArrayInputStream(bos.toByteArray()));
        check(fmt.getType() == types[0], "getMidiFileFormat type");
        check(fmt.getByteLength() == MidiFileFormat.UNKNOWN_LENGTH || fmt.getByteLength() > 0, "getMidiFileFormat byte length known or unknown");
        check(fmt.getResolution() == 96, "getMidiFileFormat resolution");

        Path dir = Files.createTempDirectory("jck");
        try {
            File out = dir.resolve("roundtrip.mid").toFile();
            check(MidiSystem.write(seq, types[0], out) > 0, "write to File positive");
            Sequence fromFile = MidiSystem.getSequence(out);
            check(fromFile != null && fromFile.getResolution() == 96, "getSequence(File) round-trip");
            MidiFileFormat fmtF = MidiSystem.getMidiFileFormat(out);
            check(fmtF.getType() == types[0], "getMidiFileFormat(File) type");
            check(fmtF.getByteLength() > 0, "getMidiFileFormat(File) byte length positive");
        } finally {
            try { Files.deleteIfExists(dir.resolve("roundtrip.mid")); } catch (IOException ignore) {}
            try { Files.deleteIfExists(dir); } catch (IOException ignore) {}
        }

        expectThrows(NullPointerException.class, () -> MidiSystem.getMidiDevice(null), "getMidiDevice(null) NPE");
        expectThrows(IllegalArgumentException.class, () -> MidiSystem.getMidiDevice(new InfoX("bogus")), "getMidiDevice unknown info IAE");
        expectThrows(NullPointerException.class, () -> MidiSystem.getSequence((InputStream) null), "getSequence(null) NPE");
        expectThrows(java.io.IOException.class, () -> MidiSystem.getSequence(new ByteArrayInputStream(new byte[0])), "getSequence empty stream IOException");
        expectThrows(NullPointerException.class, () -> MidiSystem.getMidiFileFormat((InputStream) null), "getMidiFileFormat(null) NPE");
        expectThrows(java.io.IOException.class, () -> MidiSystem.getMidiFileFormat(new ByteArrayInputStream(new byte[0])), "getMidiFileFormat empty stream IOException");
        expectThrows(InvalidMidiDataException.class, () -> MidiSystem.getSoundbank(new ByteArrayInputStream(new byte[0])), "getSoundbank empty stream InvalidMidiDataException");
        expectThrows(IllegalArgumentException.class, () -> MidiSystem.write(seq, -999, new ByteArrayOutputStream()), "write unsupported type IAE");

        try {
            Sequencer sq = MidiSystem.getSequencer();
            check(sq != null, "getSequencer non-null");
            check(sq instanceof MidiDevice, "sequencer is MidiDevice");
            if (sq.isOpen()) sq.close();
        } catch (MidiUnavailableException e) {
            check(true, "getSequencer unavailable (degraded)");
        }
        try {
            Synthesizer syn = MidiSystem.getSynthesizer();
            check(syn != null, "getSynthesizer non-null");
            if (syn.isOpen()) syn.close();
        } catch (MidiUnavailableException e) {
            check(true, "getSynthesizer unavailable (degraded)");
        }
        try {
            Receiver r = MidiSystem.getReceiver();
            check(r != null, "getReceiver non-null");
            r.close();
        } catch (MidiUnavailableException e) {
            check(true, "getReceiver unavailable (degraded)");
        }
        try {
            Transmitter tx = MidiSystem.getTransmitter();
            check(tx != null, "getTransmitter non-null");
            tx.close();
        } catch (MidiUnavailableException e) {
            check(true, "getTransmitter unavailable (degraded)");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class InfoX extends MidiDevice.Info {
    InfoX(String n) { super(n, n, n, n); }
}
